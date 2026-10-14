package cn.linguaverse.course.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.dto.CourseViews.*;
import cn.linguaverse.course.entity.*;
import cn.linguaverse.course.mapper.*;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.learn.service.ProgressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CourseService {

    private final LanguageMapper languageMapper;
    private final LevelMapper levelMapper;
    private final UnitMapper unitMapper;
    private final LessonMapper lessonMapper;
    private final LessonVocabMapper vocabMapper;
    private final GrammarPointMapper grammarPointMapper;
    private final GrammarQuestionMapper grammarQuestionMapper;
    private final ListeningMaterialMapper listeningMapper;
    private final SpeakingSentenceMapper speakingMapper;
    private final LessonStageProgressMapper stageProgressMapper;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper;

    public CourseService(LanguageMapper languageMapper, LevelMapper levelMapper, UnitMapper unitMapper,
                         LessonMapper lessonMapper, LessonVocabMapper vocabMapper,
                         GrammarPointMapper grammarPointMapper, GrammarQuestionMapper grammarQuestionMapper,
                         ListeningMaterialMapper listeningMapper, SpeakingSentenceMapper speakingMapper,
                         LessonStageProgressMapper stageProgressMapper,
                         EnrollmentService enrollmentService, ProgressService progressService,
                         ObjectMapper objectMapper) {
        this.languageMapper = languageMapper;
        this.levelMapper = levelMapper;
        this.unitMapper = unitMapper;
        this.lessonMapper = lessonMapper;
        this.vocabMapper = vocabMapper;
        this.grammarPointMapper = grammarPointMapper;
        this.grammarQuestionMapper = grammarQuestionMapper;
        this.listeningMapper = listeningMapper;
        this.speakingMapper = speakingMapper;
        this.stageProgressMapper = stageProgressMapper;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.objectMapper = objectMapper;
    }

    /** 语种列表（含 L1–L6 级别与 released 标记，不含单元/课时）。 */
    public List<LanguageVO> listLanguages(long userId) {
        List<Language> langs = languageMapper.selectList(new QueryWrapper<Language>().orderByAsc("sort_no"));
        Map<String, Integer> placementByLang = new HashMap<>();
        for (Enrollment e : enrollmentService.listByUser(userId)) {
            placementByLang.put(e.getLangCode(), e.getPlacementLevel());
        }
        List<LanguageVO> result = new ArrayList<>();
        for (Language lang : langs) {
            boolean enrolled = placementByLang.containsKey(lang.getCode());
            result.add(new LanguageVO(lang.getCode(), lang.getNameZh(), lang.getNameNative(),
                    lang.getColor(), lang.getSortNo(), enrolled,
                    enrolled ? placementByLang.get(lang.getCode()) : null,
                    levelsWithoutUnits(lang.getCode())));
        }
        return result;
    }

    /** 单个语种完整课程树，含用户完成/解锁状态。 */
    public LanguageVO courseTree(long userId, String langCode) {
        Language lang = languageMapper.selectById(langCode);
        if (lang == null) {
            throw BizException.notFound("语种不存在: " + langCode);
        }
        Enrollment enrollment = enrollmentService.get(userId, langCode);
        Set<Long> completed = progressService.completedLessonIds(userId, langCode);

        List<LevelNodeVO> levelNodes = new ArrayList<>();
        List<Level> levels = levelMapper.selectList(
                new QueryWrapper<Level>().eq("lang_code", langCode).orderByAsc("level_no"));
        for (Level level : levels) {
            List<Unit> units = unitMapper.selectList(
                    new QueryWrapper<Unit>().eq("level_id", level.getId()).orderByAsc("unit_no"));
            List<UnitNodeVO> unitNodes = new ArrayList<>();
            for (Unit unit : units) {
                List<Lesson> lessons = lessonMapper.selectList(
                        new QueryWrapper<Lesson>().eq("unit_id", unit.getId()).orderByAsc("seq_no"));
                List<LessonNodeVO> lessonNodes = new ArrayList<>();
                for (Lesson lesson : lessons) {
                    boolean done = completed.contains(lesson.getId());
                    boolean unlocked = progressService.isUnlocked(userId, lesson);
                    lessonNodes.add(new LessonNodeVO(lesson.getId(), lesson.getLessonNo(),
                            lesson.getSeqNo(), lesson.getSlug(), lesson.getTitle(),
                            lesson.getTitleNative(), done, unlocked));
                }
                unitNodes.add(new UnitNodeVO(unit.getUnitNo(), unit.getTitle(),
                        unit.getTitleNative(), lessonNodes));
            }
            levelNodes.add(new LevelNodeVO(level.getLevelNo(), level.getTitle(),
                    Boolean.TRUE.equals(level.getReleased()), unitNodes));
        }

        return new LanguageVO(lang.getCode(), lang.getNameZh(), lang.getNameNative(),
                lang.getColor(), lang.getSortNo(), enrollment != null,
                enrollment == null ? null : enrollment.getPlacementLevel(), levelNodes);
    }

    /**
     * 课时详情：未加入语种或课时未解锁 → 403；课时不存在或未发布 → 404。
     * 响应绝不包含 answer_index / 题目解析。
     */
    public LessonDetailVO lessonDetail(long userId, long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || !Boolean.TRUE.equals(lesson.getReleased())) {
            throw BizException.notFound("课时不存在");
        }
        Enrollment enrollment = enrollmentService.get(userId, lesson.getLangCode());
        if (enrollment == null) {
            throw BizException.forbidden("请先加入该语种的学习");
        }
        if (!progressService.isUnlocked(userId, lesson)) {
            throw BizException.forbidden("请先完成上一节课再解锁本课");
        }

        List<LessonVocab> vocabs = vocabMapper.selectList(
                new QueryWrapper<LessonVocab>().eq("lesson_id", lessonId).orderByAsc("sort_no"));
        List<VocabVO> vocabVOs = vocabs.stream().map(v -> new VocabVO(
                v.getWord(), v.getPhonetic(), v.getMeaningZh(), v.getExample(), v.getExampleZh())).toList();

        GrammarPoint gp = grammarPointMapper.selectOne(
                new QueryWrapper<GrammarPoint>().eq("lesson_id", lessonId));
        List<GrammarQuestion> gqs = grammarQuestionMapper.selectList(
                new QueryWrapper<GrammarQuestion>().eq("lesson_id", lessonId).orderByAsc("sort_no"));
        List<QuestionVO> gqVOs = gqs.stream()
                .map(q -> new QuestionVO(q.getId(), q.getSortNo(), q.getStem(), parseOptions(q.getOptionsJson())))
                .toList();
        GrammarVO grammarVO = new GrammarVO(gp.getTitle(), gp.getExplanation(), gqVOs);

        ListeningMaterial lm = listeningMapper.selectOne(
                new QueryWrapper<ListeningMaterial>().eq("lesson_id", lessonId));
        ListeningVO listeningVO = new ListeningVO(lm.getId(), lm.getTitle(),
                parseLines(lm.getLinesJson()), lm.getQuestionStem(), parseOptions(lm.getOptionsJson()));

        List<SpeakingSentence> sentences = speakingMapper.selectList(
                new QueryWrapper<SpeakingSentence>().eq("lesson_id", lessonId).orderByAsc("sort_no"));
        List<SpeakingVO> speakingVOs = sentences.stream()
                .map(s -> new SpeakingVO(s.getId(), s.getSortNo(), s.getText(), s.getTextZh())).toList();

        List<LessonStageProgress> stages = stageProgressMapper.selectList(
                new QueryWrapper<LessonStageProgress>().eq("user_id", userId).eq("lesson_id", lessonId)
                        .select("stage"));
        List<String> completedStages = stages.stream().map(LessonStageProgress::getStage)
                .sorted(Comparator.naturalOrder()).toList();

        return new LessonDetailVO(lesson.getId(), lesson.getLangCode(), lesson.getSeqNo(),
                lesson.getSlug(), lesson.getTitle(), lesson.getTitleNative(), lesson.getScene(),
                true, progressService.isCompleted(userId, lessonId),
                vocabVOs, grammarVO, listeningVO, speakingVOs, completedStages);
    }

    private List<LevelNodeVO> levelsWithoutUnits(String langCode) {
        return levelMapper.selectList(new QueryWrapper<Level>()
                        .eq("lang_code", langCode).orderByAsc("level_no")).stream()
                .map(l -> new LevelNodeVO(l.getLevelNo(), l.getTitle(),
                        Boolean.TRUE.equals(l.getReleased()), List.of()))
                .toList();
    }

    private List<String> parseOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("选项 JSON 解析失败", e);
        }
    }

    private List<LineVO> parseLines(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<LineVO>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("听力逐句 JSON 解析失败", e);
        }
    }
}
