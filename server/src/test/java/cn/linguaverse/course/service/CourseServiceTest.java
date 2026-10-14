package cn.linguaverse.course.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.dto.CourseViews.LanguageVO;
import cn.linguaverse.course.dto.CourseViews.LessonDetailVO;
import cn.linguaverse.course.entity.GrammarPoint;
import cn.linguaverse.course.entity.GrammarQuestion;
import cn.linguaverse.course.entity.Language;
import cn.linguaverse.course.entity.Level;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.ListeningMaterial;
import cn.linguaverse.course.entity.SpeakingSentence;
import cn.linguaverse.course.entity.Unit;
import cn.linguaverse.course.mapper.GrammarPointMapper;
import cn.linguaverse.course.mapper.GrammarQuestionMapper;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.course.mapper.LevelMapper;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.course.mapper.ListeningMaterialMapper;
import cn.linguaverse.course.mapper.SpeakingSentenceMapper;
import cn.linguaverse.course.mapper.UnitMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.learn.service.ProgressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Task 12 回归：listLanguages 的 enrolled/placementLevel 必须来自真实选学关系
 * （曾硬编码 false/null，与 courseTree 不一致）；课程树状态与课时详情权限/答案隔离。
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    private static final long USER_ID = 9L;

    @Mock LanguageMapper languageMapper;
    @Mock LevelMapper levelMapper;
    @Mock UnitMapper unitMapper;
    @Mock LessonMapper lessonMapper;
    @Mock LessonVocabMapper vocabMapper;
    @Mock GrammarPointMapper grammarPointMapper;
    @Mock GrammarQuestionMapper grammarQuestionMapper;
    @Mock ListeningMaterialMapper listeningMapper;
    @Mock SpeakingSentenceMapper speakingMapper;
    @Mock LessonStageProgressMapper stageProgressMapper;
    @Mock EnrollmentService enrollmentService;
    @Mock ProgressService progressService;

    CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(languageMapper, levelMapper, unitMapper, lessonMapper,
                vocabMapper, grammarPointMapper, grammarQuestionMapper, listeningMapper,
                speakingMapper, stageProgressMapper, enrollmentService, progressService,
                new ObjectMapper());
    }

    private Language lang(String code, int sortNo) {
        Language l = new Language();
        l.setCode(code);
        l.setNameZh(code + "语");
        l.setNameNative(code.toUpperCase());
        l.setColor("#000000");
        l.setSortNo(sortNo);
        return l;
    }

    private Enrollment enrollment(String code, int level) {
        Enrollment e = new Enrollment();
        e.setUserId(USER_ID);
        e.setLangCode(code);
        e.setPlacementLevel(level);
        return e;
    }

    @Test
    @DisplayName("listLanguages 回归：已选学语种 enrolled=true 且带真实定级级别，未选学为 false/null")
    void listLanguagesReflectsRealEnrollments() {
        when(languageMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(lang("en", 1), lang("ja", 2), lang("ko", 3)));
        when(enrollmentService.listByUser(USER_ID))
                .thenReturn(List.of(enrollment("en", 3), enrollment("ko", 1)));
        when(levelMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());

        List<LanguageVO> vos = service.listLanguages(USER_ID);

        assertThat(vos).hasSize(3);
        LanguageVO en = vos.get(0);
        assertThat(en.enrolled()).isTrue();
        assertThat(en.placementLevel()).isEqualTo(3);
        LanguageVO ja = vos.get(1);
        assertThat(ja.enrolled()).isFalse();
        assertThat(ja.placementLevel()).isNull();
        LanguageVO ko = vos.get(2);
        assertThat(ko.enrolled()).isTrue();
        assertThat(ko.placementLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("listLanguages：级别 released 标记与空单元列表")
    void listLanguagesLevelFlags() {
        when(languageMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(lang("en", 1)));
        when(enrollmentService.listByUser(USER_ID)).thenReturn(List.of());
        Level released = new Level();
        released.setLevelNo(1);
        released.setTitle("入门");
        released.setReleased(true);
        Level locked = new Level();
        locked.setLevelNo(4);
        locked.setTitle("高级");
        locked.setReleased(false);
        when(levelMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(released, locked));

        LanguageVO vo = service.listLanguages(USER_ID).get(0);

        assertThat(vo.levels()).hasSize(2);
        assertThat(vo.levels().get(0).released()).isTrue();
        assertThat(vo.levels().get(0).units()).isEmpty();
        assertThat(vo.levels().get(1).released()).isFalse();
    }

    @Test
    @DisplayName("courseTree：课时完成/解锁状态与级别 released 正确映射")
    void courseTreeMapsStates() {
        when(languageMapper.selectById("en")).thenReturn(lang("en", 1));
        when(enrollmentService.get(USER_ID, "en")).thenReturn(enrollment("en", 2));
        when(progressService.completedLessonIds(USER_ID, "en"))
                .thenReturn(java.util.Set.of(11L));

        Level level = new Level();
        level.setId(1L);
        level.setLevelNo(1);
        level.setReleased(true);
        when(levelMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(level));

        Unit unit = new Unit();
        unit.setId(2L);
        unit.setUnitNo(1);
        when(unitMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(unit));

        Lesson done = lesson(11L, 1);
        Lesson open = lesson(12L, 2);
        when(progressService.isUnlocked(USER_ID, done)).thenReturn(true);
        when(progressService.isUnlocked(USER_ID, open)).thenReturn(true);

        Lesson closed = lesson(13L, 3);
        when(progressService.isUnlocked(USER_ID, closed)).thenReturn(false);
        when(lessonMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(done, open, closed));

        LanguageVO tree = service.courseTree(USER_ID, "en");

        assertThat(tree.enrolled()).isTrue();
        assertThat(tree.placementLevel()).isEqualTo(2);
        var nodes = tree.levels().get(0).units().get(0).lessons();
        assertThat(nodes).extracting(n -> n.completed()).containsExactly(true, false, false);
        assertThat(nodes).extracting(n -> n.unlocked()).containsExactly(true, true, false);
    }

    @Test
    @DisplayName("courseTree：语种不存在 → 404")
    void courseTreeUnknownLang404() {
        when(languageMapper.selectById("xx")).thenReturn(null);
        assertThatThrownBy(() -> service.courseTree(USER_ID, "xx"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("课时详情：不存在 / 未发布 → 404")
    void lessonDetailNotFound404() {
        when(lessonMapper.selectById(1L)).thenReturn(null);
        assertThatThrownBy(() -> service.lessonDetail(USER_ID, 1L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));

        Lesson unreleased = lesson(1L, 1);
        unreleased.setReleased(false);
        when(lessonMapper.selectById(1L)).thenReturn(unreleased);
        assertThatThrownBy(() -> service.lessonDetail(USER_ID, 1L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("课时详情：未选学 → 403")
    void lessonDetailNotEnrolled403() {
        when(lessonMapper.selectById(1L)).thenReturn(lesson(1L, 1));
        when(enrollmentService.get(USER_ID, "en")).thenReturn(null);
        assertThatThrownBy(() -> service.lessonDetail(USER_ID, 1L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("课时详情：未解锁 → 403")
    void lessonDetailLocked403() {
        Lesson l = lesson(1L, 1);
        when(lessonMapper.selectById(1L)).thenReturn(l);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(enrollment("en", 1));
        when(progressService.isUnlocked(USER_ID, l)).thenReturn(false);

        assertThatThrownBy(() -> service.lessonDetail(USER_ID, 1L))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("课时详情：组装成功且序列化不含任何答案字段（answerIndex）")
    void lessonDetailExcludesAnswers() throws Exception {
        Lesson l = lesson(1L, 1);
        l.setScene("cafe");
        when(lessonMapper.selectById(1L)).thenReturn(l);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(enrollment("en", 1));
        when(progressService.isUnlocked(USER_ID, l)).thenReturn(true);
        when(progressService.isCompleted(USER_ID, 1L)).thenReturn(false);

        cn.linguaverse.course.entity.LessonVocab v = new cn.linguaverse.course.entity.LessonVocab();
        v.setWord("hello");
        v.setPhonetic("/həˈloʊ/");
        v.setMeaningZh("你好");
        when(vocabMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(v));

        GrammarPoint gp = new GrammarPoint();
        gp.setTitle("be 动词");
        gp.setExplanation("am/is/are 的用法");
        when(grammarPointMapper.selectOne(any(QueryWrapper.class))).thenReturn(gp);

        GrammarQuestion q = new GrammarQuestion();
        q.setId(101L);
        q.setSortNo(1);
        q.setStem("I ___ a student.");
        q.setOptionsJson("[\"am\",\"is\",\"are\",\"be\"]");
        q.setAnswerIndex(0);
        when(grammarQuestionMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(q));

        ListeningMaterial lm = new ListeningMaterial();
        lm.setId(201L);
        lm.setTitle("点咖啡");
        lm.setLinesJson("[{\"speaker\":\"A\",\"text\":\"One coffee\",\"zh\":\"一杯咖啡\"}]");
        lm.setQuestionStem("What does she order?");
        lm.setOptionsJson("[\"tea\",\"coffee\",\"water\"]");
        lm.setAnswerIndex(1);
        when(listeningMapper.selectOne(any(QueryWrapper.class))).thenReturn(lm);

        SpeakingSentence s = new SpeakingSentence();
        s.setId(301L);
        s.setSortNo(1);
        s.setText("Nice to meet you");
        s.setTextZh("很高兴见到你");
        when(speakingMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(s));

        LessonStageProgress sp = new LessonStageProgress();
        sp.setStage("VOCAB");
        LessonStageProgress sp2 = new LessonStageProgress();
        sp2.setStage("GRAMMAR");
        when(stageProgressMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(sp2, sp)); // 乱序插入，验证排序

        LessonDetailVO detail = service.lessonDetail(USER_ID, 1L);

        assertThat(detail.unlocked()).isTrue();
        assertThat(detail.completedStages()).containsExactly("GRAMMAR", "VOCAB");
        assertThat(detail.grammar().questions().get(0).options()).hasSize(4);
        assertThat(detail.listening().questionStem()).isEqualTo("What does she order?");

        String json = new ObjectMapper().writeValueAsString(detail);
        assertThat(json).doesNotContain("answerIndex").doesNotContain("\"answer\"");
    }

    private Lesson lesson(long id, int seqNo) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setUnitId(2L);
        lesson.setLessonNo(seqNo);
        lesson.setSeqNo(seqNo);
        lesson.setSlug("en-l" + seqNo);
        lesson.setTitle("L" + seqNo);
        lesson.setTitleNative("L" + seqNo + " native");
        lesson.setLangCode("en");
        lesson.setReleased(true);
        return lesson;
    }
}
