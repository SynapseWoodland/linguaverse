package cn.linguaverse.seed;

import cn.linguaverse.community.entity.Comment;
import cn.linguaverse.community.entity.Post;
import cn.linguaverse.community.mapper.CommentMapper;
import cn.linguaverse.community.mapper.PostMapper;
import cn.linguaverse.course.entity.*;
import cn.linguaverse.course.mapper.*;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 课程内容与社区种子灌种器。幂等：languages 已有数据则整体跳过。
 */
@Component
@Order(10)
public class ContentSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentSeeder.class);

    private final LanguageMapper languageMapper;
    private final LevelMapper levelMapper;
    private final UnitMapper unitMapper;
    private final LessonMapper lessonMapper;
    private final LessonVocabMapper vocabMapper;
    private final GrammarPointMapper grammarPointMapper;
    private final GrammarQuestionMapper grammarQuestionMapper;
    private final ListeningMaterialMapper listeningMapper;
    private final SpeakingSentenceMapper speakingMapper;
    private final PlacementQuestionMapper placementMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public ContentSeeder(LanguageMapper languageMapper, LevelMapper levelMapper, UnitMapper unitMapper,
                         LessonMapper lessonMapper, LessonVocabMapper vocabMapper,
                         GrammarPointMapper grammarPointMapper, GrammarQuestionMapper grammarQuestionMapper,
                         ListeningMaterialMapper listeningMapper, SpeakingSentenceMapper speakingMapper,
                         PlacementQuestionMapper placementMapper, UserMapper userMapper,
                         PostMapper postMapper, CommentMapper commentMapper,
                         PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.languageMapper = languageMapper;
        this.levelMapper = levelMapper;
        this.unitMapper = unitMapper;
        this.lessonMapper = lessonMapper;
        this.vocabMapper = vocabMapper;
        this.grammarPointMapper = grammarPointMapper;
        this.grammarQuestionMapper = grammarQuestionMapper;
        this.listeningMapper = listeningMapper;
        this.speakingMapper = speakingMapper;
        this.placementMapper = placementMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long langCount = languageMapper.selectCount(null);
        if (langCount != null && langCount > 0) {
            log.info("[seed] 课程内容已存在（languages={}），跳过灌种", langCount);
            return;
        }

        List<LangBundle> bundles = List.of(
            new LangBundle("en", "英语", "English", "#4F7CFF", EnContent.levels(), EnContent.placement()),
            new LangBundle("ja", "日语", "日本語", "#E0587B", JaContent.levels(), JaContent.placement()),
            new LangBundle("ko", "韩语", "한국어", "#2FA98C", KoContent.levels(), KoContent.placement())
        );

        for (LangBundle b : bundles) {
            validate(b);
        }

        for (int i = 0; i < bundles.size(); i++) {
            seedLanguage(bundles.get(i), i + 1);
        }

        Long demoId = seedDemoUser();
        seedCommunity(demoId);

        logCounts();
        log.info("[seed] 灌种完成。演示账号 demo / demo1234");
    }

    // ---------------- 灌种 ----------------

    private void seedLanguage(LangBundle b, int sortNo) {
        Language lang = new Language();
        lang.setCode(b.code);
        lang.setNameZh(b.nameZh);
        lang.setNameNative(b.nameNative);
        lang.setColor(b.color);
        lang.setSortNo(sortNo);
        languageMapper.insert(lang);

        // L1-L6：语料只覆盖 L1-L3，L4-L6 为 upcoming 占位
        for (int levelNo = 1; levelNo <= 6; levelNo++) {
            Level level = new Level();
            level.setLangCode(b.code);
            level.setLevelNo(levelNo);
            level.setTitle(levelNo <= 3 ? b.levels.get(levelNo - 1).title() : upcomingTitle(levelNo));
            level.setReleased(levelNo <= 3);
            levelMapper.insert(level);
        }

        int seq = 0;
        for (ContentSpecs.Level lvl : b.levels) {
            Level levelEntity = findLevel(b.code, lvl.no());
            int unitNo = 0;
            for (ContentSpecs.Unit unit : lvl.units()) {
                unitNo++;
                Unit u = new Unit();
                u.setLevelId(levelEntity.getId());
                u.setUnitNo(unitNo);
                u.setTitle(unit.title());
                u.setTitleNative(unit.nativeTitle());
                unitMapper.insert(u);

                int lessonNo = 0;
                for (ContentSpecs.Lesson ls : unit.lessons()) {
                    lessonNo++;
                    seq++;
                    Lesson lesson = new Lesson();
                    lesson.setUnitId(u.getId());
                    lesson.setLessonNo(lessonNo);
                    lesson.setLangCode(b.code);
                    lesson.setSeqNo(seq);
                    lesson.setSlug(normalizeSlug(ls.slug()));
                    lesson.setTitle(ls.title());
                    lesson.setTitleNative(ls.nativeTitle());
                    lesson.setScene(ls.scene());
                    lesson.setReleased(true);
                    lessonMapper.insert(lesson);

                    seedLessonContent(lesson.getId(), ls);
                }
            }
        }

        int sort = 0;
        for (ContentSpecs.PlacementQ pq : b.placement()) {
            sort++;
            PlacementQuestion q = new PlacementQuestion();
            q.setLangCode(b.code);
            q.setSortNo(sort);
            q.setStem(pq.stem());
            q.setOptionsJson(toJson(pq.options()));
            q.setAnswerIndex(pq.answer());
            placementMapper.insert(q);
        }
    }

    private void seedLessonContent(Long lessonId, ContentSpecs.Lesson ls) {
        int sort = 0;
        for (ContentSpecs.Vocab v : ls.vocab()) {
            sort++;
            LessonVocab e = new LessonVocab();
            e.setLessonId(lessonId);
            e.setWord(v.word());
            e.setPhonetic(v.phonetic());
            e.setMeaningZh(v.zh());
            e.setExample(v.ex());
            e.setExampleZh(v.exZh());
            e.setSortNo(sort);
            vocabMapper.insert(e);
        }

        GrammarPoint gp = new GrammarPoint();
        gp.setLessonId(lessonId);
        gp.setTitle(ls.grammarTitle());
        gp.setExplanation(ls.grammarBody());
        grammarPointMapper.insert(gp);

        sort = 0;
        for (ContentSpecs.GrammarQ gq : ls.grammar()) {
            sort++;
            GrammarQuestion e = new GrammarQuestion();
            e.setLessonId(lessonId);
            e.setStem(gq.stem());
            e.setOptionsJson(toJson(gq.options()));
            e.setAnswerIndex(gq.answer());
            e.setExplanation(gq.explain());
            e.setSortNo(sort);
            grammarQuestionMapper.insert(e);
        }

        ContentSpecs.Listening li = ls.listening();
        ListeningMaterial lm = new ListeningMaterial();
        lm.setLessonId(lessonId);
        lm.setTitle(li.title());
        lm.setLinesJson(toJson(li.lines()));
        lm.setQuestionStem(li.stem());
        lm.setOptionsJson(toJson(li.options()));
        lm.setAnswerIndex(li.answer());
        lm.setExplanation(li.explain());
        listeningMapper.insert(lm);

        sort = 0;
        for (ContentSpecs.Speaking sp : ls.speaking()) {
            sort++;
            SpeakingSentence e = new SpeakingSentence();
            e.setLessonId(lessonId);
            e.setText(sp.text());
            e.setTextZh(sp.zh());
            e.setSortNo(sort);
            speakingMapper.insert(e);
        }
    }

    private Long seedDemoUser() {
        User existing = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", "demo"));
        if (existing != null) {
            return existing.getId();
        }
        User u = new User();
        u.setUsername("demo");
        u.setPasswordHash(passwordEncoder.encode("demo1234"));
        u.setNickname("示范学员");
        u.setBio("这是一个用于体验社区的示范账号。");
        u.setAvatarSeed("D");
        u.setRole("USER");
        u.setDailyGoalXp(30);
        u.setStreak(0);
        u.setOnboarded(true);
        userMapper.insert(u);
        return u.getId();
    }

    private void seedCommunity(Long demoId) {
        record SeedPost(String lang, String title, String content, List<String> comments) {}

        List<SeedPost> posts = List.of(
            new SeedPost("en", "How do you memorize irregular verbs?",
                "I keep mixing up go/went/gone. Writing sentences with each form helps a little. What works for you?",
                List.of("Try grouping them by rhyme: teach/taught, catch/caught!",
                        "I record myself saying all three forms every morning.")),
            new SeedPost("ja", "は と が の違いがいつも分かりません",
                "教科書を読んでも、結局どっちを使うか迷います。コツがあれば教えてください！",
                List.of("新しい話題を出すときは は、その場で初めて言うときは が、が分かりやすいです。")),
            new SeedPost("ko", "드라마 대사로 공부하는 분 있나요?",
                "자막 없이 보고 싶어서 매일 한 문장씩 받아쓰기 하고 있어요. 두 달 되니까 조금 들리기 시작했어요.",
                List.of("저도 같은 방법으로 해요! 배우의 대사를 그림자처럼 따라 하면 발음도 좋아져요.")),
            new SeedPost(null, "打卡第 7 天，真的会成习惯",
                "从最开始 5 分钟都坐不住，到现在每天早上先复习到期单词。大家也加油！",
                List.of()),
            new SeedPost("en", "Shadowing tips for beginners?",
                "Should I shadow with the video playing at the same time, or listen first and repeat after a pause?",
                List.of("Pause-and-repeat first; shadowing live is harder than it looks."))
        );

        for (SeedPost sp : posts) {
            Post p = new Post();
            p.setAuthorId(demoId);
            p.setLangCode(sp.lang());
            p.setTitle(sp.title());
            p.setContent(sp.content());
            p.setLikeCount(0);
            p.setCommentCount(sp.comments().size());
            p.setHidden(false);
            p.setDeleted(false);
            postMapper.insert(p);

            for (String text : sp.comments()) {
                Comment c = new Comment();
                c.setPostId(p.getId());
                c.setAuthorId(demoId);
                c.setContent(text);
                commentMapper.insert(c);
            }
        }
    }

    // ---------------- 自检 ----------------

    private void validate(LangBundle b) {
        List<String> errors = new ArrayList<>();
        Set<String> slugs = new HashSet<>();
        int lessonCount = 0;
        int seqExpect = 0;

        if (b.levels().size() != 3) {
            errors.add(b.code + " levels 数量应为 3，实际 " + b.levels().size());
        }
        for (ContentSpecs.Level lvl : b.levels()) {
            if (lvl.units().size() != 2) {
                errors.add(b.code + " L" + lvl.no() + " 单元数应为 2");
            }
            for (ContentSpecs.Unit u : lvl.units()) {
                if (u.lessons().size() != 2) {
                    errors.add(b.code + " L" + lvl.no() + " 单元「" + u.title() + "」课时数应为 2");
                }
                for (ContentSpecs.Lesson ls : u.lessons()) {
                    lessonCount++;
                    seqExpect++;
                    String slug = normalizeSlug(ls.slug());
                    if (!slugs.add(b.code + ":" + slug)) {
                        errors.add(b.code + " slug 重复: " + slug);
                    }
                    if (ls.vocab().size() != 8) {
                        errors.add(b.code + " " + slug + " 词汇数=" + ls.vocab().size() + "，应为 8");
                    }
                    if (ls.grammar().size() != 4) {
                        errors.add(b.code + " " + slug + " 语法题数=" + ls.grammar().size() + "，应为 4");
                    }
                    for (ContentSpecs.GrammarQ gq : ls.grammar()) {
                        checkOptions(b.code, slug, gq.options(), gq.answer(), errors);
                    }
                    if (ls.listening() == null) {
                        errors.add(b.code + " " + slug + " 缺少听力材料");
                    } else {
                        if (ls.listening().lines().isEmpty()) {
                            errors.add(b.code + " " + slug + " 听力逐句为空");
                        }
                        checkOptions(b.code, slug, ls.listening().options(), ls.listening().answer(), errors);
                    }
                    if (ls.speaking().size() != 5) {
                        errors.add(b.code + " " + slug + " 跟读句数=" + ls.speaking().size() + "，应为 5");
                    }
                }
            }
        }
        if (lessonCount != 12) {
            errors.add(b.code + " 课时总数=" + lessonCount + "，应为 12");
        }
        if (b.placement().size() != 10) {
            errors.add(b.code + " 定级题数=" + b.placement().size() + "，应为 10");
        }
        for (ContentSpecs.PlacementQ pq : b.placement()) {
            checkOptions(b.code, "placement", pq.options(), pq.answer(), errors);
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("语料自检失败：\n  " + String.join("\n  ", errors));
        }
        log.info("[seed] {} 语料自检通过：12 课时 / 96 词 / 48 语法题 / 10 定级题", b.code);
    }

    private void checkOptions(String lang, String where, List<String> options, int answer, List<String> errors) {
        if (options.size() != 4) {
            errors.add(lang + " " + where + " 选项数=" + options.size() + "，应为 4");
        }
        if (answer < 0 || answer >= options.size()) {
            errors.add(lang + " " + where + " answerIndex=" + answer + " 越界");
        }
    }

    private void logCounts() {
        log.info("[seed] 数据量自检：languages={}, levels={}, units={}, lessons={}, vocab={}, grammarPoints={}, grammarQuestions={}, listening={}, speaking={}, placement={}, posts={}, comments={}",
            languageMapper.selectCount(null),
            levelMapper.selectCount(null),
            unitMapper.selectCount(null),
            lessonMapper.selectCount(null),
            vocabMapper.selectCount(null),
            grammarPointMapper.selectCount(null),
            grammarQuestionMapper.selectCount(null),
            listeningMapper.selectCount(null),
            speakingMapper.selectCount(null),
            placementMapper.selectCount(null),
            postMapper.selectCount(null),
            commentMapper.selectCount(null));
    }

    // ---------------- 工具 ----------------

    private Level findLevel(String code, int levelNo) {
        return levelMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Level>()
                .eq("lang_code", code).eq("level_no", levelNo));
    }

    /** ja-/ko- 前缀在 (lang_code, slug) 唯一键下是冗余的，统一去掉。 */
    private static String normalizeSlug(String slug) {
        if (slug.startsWith("ja-") || slug.startsWith("ko-")) {
            return slug.substring(3);
        }
        return slug;
    }

    private static String upcomingTitle(int levelNo) {
        return switch (levelNo) {
            case 4 -> "进阶拓展（敬请期待）";
            case 5 -> "高阶应用（敬请期待）";
            default -> "精通冲刺（敬请期待）";
        };
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 序列化失败: " + value, e);
        }
    }

    private record LangBundle(String code, String nameZh, String nameNative, String color,
                              List<ContentSpecs.Level> levels, List<ContentSpecs.PlacementQ> placement) {}
}
