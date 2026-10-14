package cn.linguaverse.integration;

import cn.linguaverse.course.dto.CourseViews.LanguageVO;
import cn.linguaverse.course.dto.CourseViews.LessonNodeVO;
import cn.linguaverse.course.dto.PlacementViews.ResultVO;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.LessonVocab;
import cn.linguaverse.course.entity.PlacementQuestion;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.course.mapper.PlacementQuestionMapper;
import cn.linguaverse.course.service.CourseService;
import cn.linguaverse.course.service.PlacementService;
import cn.linguaverse.learn.dto.CompletionViews.CompleteVO;
import cn.linguaverse.learn.dto.ReviewViews.DueQueueVO;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import cn.linguaverse.learn.service.CompletionService;
import cn.linguaverse.learn.service.SrsService;
import cn.linguaverse.seed.ContentSeeder;
import cn.linguaverse.user.dto.RegisterRequest;
import cn.linguaverse.user.dto.UserViews.AuthResponse;
import cn.linguaverse.user.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试（mvn verify -Pit，Testcontainers mysql:8）：
 * 建表/灌种双幂等；定级→课程树解锁链→四环节结算 XP→重复结算幂等；到期队列排除 mastered。
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoreFlowIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("linguaverse_it")
            .withUsername("it")
            .withPassword("it123456");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("DB_HOST", mysql::getHost);
        registry.add("DB_PORT", () -> mysql.getMappedPort(3306));
        registry.add("DB_NAME", mysql::getDatabaseName);
        registry.add("DB_USER", mysql::getUsername);
        registry.add("DB_PASSWORD", mysql::getPassword);
    }

    @Autowired DataSource dataSource;
    @Autowired ContentSeeder contentSeeder;
    @Autowired LanguageMapper languageMapper;
    @Autowired LessonMapper lessonMapper;
    @Autowired LessonVocabMapper vocabMapper;
    @Autowired PlacementQuestionMapper placementQuestionMapper;
    @Autowired LessonStageProgressMapper stageProgressMapper;
    @Autowired WordMasteryMapper wordMasteryMapper;
    @Autowired AuthService authService;
    @Autowired PlacementService placementService;
    @Autowired CourseService courseService;
    @Autowired CompletionService completionService;
    @Autowired SrsService srsService;

    @Test
    @DisplayName("schema 与灌种双幂等：二次执行无异常且计数稳定（3 语种 / 36 课时 / 288 词）")
    void schemaAndSeederIdempotent() {
        long langs = languageMapper.selectCount(null);
        long lessons = lessonMapper.selectCount(null);
        long vocabs = vocabMapper.selectCount(null);
        assertThat(langs).isEqualTo(3);
        assertThat(lessons).isEqualTo(36);
        assertThat(vocabs).isEqualTo(288);

        // schema.sql 整体再执行一次
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/schema.sql"));
        } catch (Exception e) {
            throw new AssertionError("schema.sql 二次执行应幂等无报错", e);
        }

        // 灌种器再跑一次（languages 非空 → 内部跳过）
        contentSeeder.run(null);

        assertThat(languageMapper.selectCount(null)).isEqualTo(langs);
        assertThat(lessonMapper.selectCount(null)).isEqualTo(lessons);
        assertThat(vocabMapper.selectCount(null)).isEqualTo(vocabs);
    }

    @Test
    @DisplayName("核心链路：定级 L3 → 仅 seq1 解锁 → 满分结算 56XP 解锁 seq2 → 重复结算幂等 → due 排除 mastered")
    void coreChainFlow() {
        // 1. 注册真实用户
        RegisterRequest req = new RegisterRequest();
        req.setUsername("it_chain_user");
        req.setPassword("secret123");
        req.setNickname("链路用户");
        AuthResponse auth = authService.register(req);
        long userId = auth.user().id();

        // 2. 定级：直接读题库拿正确答案，10/10 → L3
        List<PlacementQuestion> questions = placementQuestionMapper.selectList(
                new QueryWrapper<PlacementQuestion>().eq("lang_code", "en")
                        .orderByAsc("sort_no", "id"));
        assertThat(questions).hasSize(10);
        List<Integer> correctAnswers = questions.stream().map(PlacementQuestion::getAnswerIndex).toList();
        ResultVO result = placementService.submit(userId, "en", correctAnswers);
        assertThat(result.correctCount()).isEqualTo(10);
        assertThat(result.suggestedLevel()).isEqualTo(3);
        assertThat(result.enrolled()).isTrue();

        // 3. 初始课程树：seq1 unlocked、seq2 locked
        List<LessonNodeVO> nodes = flattenLessons(courseService.courseTree(userId, "en"));
        LessonNodeVO first = bySeq(nodes, 1);
        assertThat(first.unlocked()).isTrue();
        assertThat(first.completed()).isFalse();
        assertThat(bySeq(nodes, 2).unlocked()).isFalse();

        // 4. 补齐四环节成绩后结算（L1 共 8 词，满分 → 36+20=56 XP）
        long lesson1Id = first.id();
        for (String stage : List.of("VOCAB", "GRAMMAR", "LISTENING", "SPEAKING")) {
            LessonStageProgress p = new LessonStageProgress();
            p.setUserId(userId);
            p.setLessonId(lesson1Id);
            p.setStage(stage);
            p.setScore(100);
            stageProgressMapper.insert(p);
        }
        CompleteVO vo = completionService.complete(userId, lesson1Id, 120);
        assertThat(vo.alreadyCompleted()).isFalse();
        assertThat(vo.accuracy()).isEqualTo(100);
        assertThat(vo.xp().total()).isEqualTo(56);
        assertThat(vo.xp().base()).isEqualTo(36);
        assertThat(vo.xp().bonus()).isEqualTo(20);
        assertThat(vo.streak()).isEqualTo(1);
        assertThat(vo.newAchievements()).contains("first_step", "perfect");

        // 5. 结算后课程树：seq1 completed、seq2 unlocked
        nodes = flattenLessons(courseService.courseTree(userId, "en"));
        assertThat(bySeq(nodes, 1).completed()).isTrue();
        assertThat(bySeq(nodes, 2).unlocked()).isTrue();

        // 6. 重复结算：幂等回放，XP 从流水还原，不再发成就
        CompleteVO replay = completionService.complete(userId, lesson1Id, 999);
        assertThat(replay.alreadyCompleted()).isTrue();
        assertThat(replay.xp().total()).isEqualTo(56);
        assertThat(replay.durationSec()).isEqualTo(120);
        assertThat(replay.newAchievements()).isEmpty();

        // 7. SRS 到期队列：stage4 评 KNOWN → mastered 退出队列；另一个词 UNKNOWN → 当天到期入队
        List<LessonVocab> words = vocabMapper.selectList(new QueryWrapper<LessonVocab>()
                .eq("lesson_id", lesson1Id).orderByAsc("sort_no", "id"));
        assertThat(words.size()).isGreaterThanOrEqualTo(2);
        LessonVocab willMaster = words.get(0);
        LessonVocab newWord = words.get(1);

        WordMastery m = new WordMastery();
        m.setUserId(userId);
        m.setLangCode("en");
        m.setWord(willMaster.getWord());
        m.setStage(4);
        m.setDueDate(LocalDate.now());
        m.setMastered(false);
        m.setPhonetic(willMaster.getPhonetic());
        m.setMeaningZh(willMaster.getMeaningZh());
        wordMasteryMapper.insert(m);
        srsService.rate(userId, "en", willMaster.getWord(), "KNOWN");

        srsService.rate(userId, "en", newWord.getWord(), "UNKNOWN");

        DueQueueVO due = srsService.due(userId, "en", null);
        List<String> dueWords = due.cards().stream().map(c -> c.word()).toList();
        assertThat(dueWords).contains(newWord.getWord());
        assertThat(dueWords).doesNotContain(willMaster.getWord());
    }

    private List<LessonNodeVO> flattenLessons(LanguageVO tree) {
        return tree.levels().stream()
                .flatMap(lv -> lv.units().stream())
                .flatMap(u -> u.lessons().stream())
                .collect(Collectors.toList());
    }

    private LessonNodeVO bySeq(List<LessonNodeVO> nodes, int seq) {
        return nodes.stream().filter(n -> n.seqNo() == seq).findFirst()
                .orElseThrow(() -> new AssertionError("缺少 seqNo=" + seq + " 的课时"));
    }
}
