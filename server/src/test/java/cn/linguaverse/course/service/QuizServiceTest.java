package cn.linguaverse.course.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.dto.QuizViews.GrammarAnswerItem;
import cn.linguaverse.course.dto.QuizViews.GrammarResultVO;
import cn.linguaverse.course.dto.QuizViews.ListeningResultVO;
import cn.linguaverse.course.dto.QuizViews.SpeakingResultVO;
import cn.linguaverse.course.dto.QuizViews.SpeakingSentenceItem;
import cn.linguaverse.course.dto.QuizViews.SpeakingSubmitRequest;
import cn.linguaverse.course.entity.GrammarQuestion;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.ListeningMaterial;
import cn.linguaverse.course.entity.SpeakingSentence;
import cn.linguaverse.course.mapper.GrammarQuestionMapper;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.ListeningMaterialMapper;
import cn.linguaverse.course.mapper.SpeakingSentenceMapper;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.learn.service.ProgressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 语法/听力/口语判题规则：分数、答案回执、流水与环节进度落库、权限与参数校验。 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    private static final long USER_ID = 9L;
    private static final long LESSON_ID = 1L;

    @Mock
    LessonMapper lessonMapper;
    @Mock
    GrammarQuestionMapper grammarQuestionMapper;
    @Mock
    ListeningMaterialMapper listeningMapper;
    @Mock
    SpeakingSentenceMapper speakingMapper;
    @Mock
    ActivityMapper activityMapper;
    @Mock
    LessonStageProgressMapper stageProgressMapper;
    @Mock
    EnrollmentService enrollmentService;
    @Mock
    ProgressService progressService;

    QuizService service;
    Lesson lesson;

    @BeforeEach
    void setUp() {
        service = new QuizService(lessonMapper, grammarQuestionMapper, listeningMapper,
                speakingMapper, activityMapper, stageProgressMapper,
                enrollmentService, progressService, new ObjectMapper());
        lesson = new Lesson();
        lesson.setId(LESSON_ID);
        lesson.setLangCode("en");
        lesson.setSeqNo(1);
        lesson.setReleased(true);
    }

    private void stubAccessible() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);
    }

    // ---------- 语法 ----------

    private List<GrammarQuestion> fourGrammarQuestions() {
        List<GrammarQuestion> list = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            GrammarQuestion q = new GrammarQuestion();
            q.setId(100L + i);
            q.setLessonId(LESSON_ID);
            q.setStem("G" + i);
            q.setOptionsJson("[\"a\",\"b\",\"c\",\"d\"]");
            q.setAnswerIndex(i - 1); // 101→0、102→1、103→2、104→3
            q.setExplanation("ex" + i);
            q.setSortNo(i);
            list.add(q);
        }
        return list;
    }

    private List<GrammarAnswerItem> grammarAnswers(int... choicesByQuestionIdOrder) {
        List<GrammarAnswerItem> answers = new ArrayList<>();
        for (int i = 0; i < choicesByQuestionIdOrder.length; i++) {
            answers.add(new GrammarAnswerItem(101L + i, choicesByQuestionIdOrder[i]));
        }
        return answers;
    }

    private void stubGrammar() {
        stubAccessible();
        when(grammarQuestionMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(fourGrammarQuestions());
        lenient().when(stageProgressMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
    }

    @Test
    @DisplayName("语法全对 → 100，逐题回执正确答案与解析，写流水与环节进度")
    void grammarAllCorrect() {
        stubGrammar();
        // 乱序提交 103/101/104/102，验证结果按 questionId 升序
        List<GrammarAnswerItem> answers = List.of(
                new GrammarAnswerItem(103L, 2),
                new GrammarAnswerItem(101L, 0),
                new GrammarAnswerItem(104L, 3),
                new GrammarAnswerItem(102L, 1));

        GrammarResultVO vo = service.gradeGrammar(USER_ID, LESSON_ID, answers);

        assertThat(vo.score()).isEqualTo(100);
        assertThat(vo.correctCount()).isEqualTo(4);
        assertThat(vo.total()).isEqualTo(4);
        assertThat(vo.results()).extracting(r -> r.questionId())
                .containsExactly(101L, 102L, 103L, 104L);
        assertThat(vo.results()).allSatisfy(r -> {
            assertThat(r.correct()).isTrue();
            assertThat(r.correctIndex()).isBetween(0, 3);
            assertThat(r.explanation()).startsWith("ex");
        });

        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).insert(activityCaptor.capture());
        Activity a = activityCaptor.getValue();
        assertThat(a.getType()).isEqualTo("GRAMMAR");
        assertThat(a.getXp()).isZero();
        assertThat(a.getScore()).isEqualTo(100);

        ArgumentCaptor<LessonStageProgress> stageCaptor = ArgumentCaptor.forClass(LessonStageProgress.class);
        verify(stageProgressMapper).insert(stageCaptor.capture());
        assertThat(stageCaptor.getValue().getStage()).isEqualTo("GRAMMAR");
        assertThat(stageCaptor.getValue().getScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("语法半对 → 50")
    void grammarHalfCorrect() {
        stubGrammar();
        GrammarResultVO vo = service.gradeGrammar(USER_ID, LESSON_ID,
                grammarAnswers(0, 1, 0, 0)); // 101、102 对；103、104 错
        assertThat(vo.correctCount()).isEqualTo(2);
        assertThat(vo.score()).isEqualTo(50);
        assertThat(vo.results()).filteredOn(r -> !r.correct()).hasSize(2);
    }

    @Test
    @DisplayName("语法作答数与题数不符 → 400")
    void grammarCountMismatch400() {
        stubGrammar();
        assertThatThrownBy(() -> service.gradeGrammar(USER_ID, LESSON_ID,
                grammarAnswers(0, 1, 2)))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("混入他课题目 → 400")
    void grammarForeignQuestion400() {
        stubGrammar();
        List<GrammarAnswerItem> answers = grammarAnswers(0, 1, 2);
        answers.add(new GrammarAnswerItem(999L, 0));
        assertThatThrownBy(() -> service.gradeGrammar(USER_ID, LESSON_ID, answers))
                .isInstanceOfSatisfying(BizException.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(400);
                    assertThat(e.getMessage()).contains("999");
                });
    }

    @Test
    @DisplayName("重复作答同一题 → 400")
    void grammarDuplicateQuestion400() {
        stubGrammar();
        List<GrammarAnswerItem> answers = grammarAnswers(0, 1, 2);
        answers.add(new GrammarAnswerItem(101L, 3));
        assertThatThrownBy(() -> service.gradeGrammar(USER_ID, LESSON_ID, answers))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("选项下标越界 → 400")
    void grammarChoiceOutOfRange400() {
        stubGrammar();
        assertThatThrownBy(() -> service.gradeGrammar(USER_ID, LESSON_ID,
                grammarAnswers(0, 1, 2, 4)))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("已有环节进度 → update 而非 insert")
    void stageProgressUpsertUpdates() {
        stubGrammar();
        LessonStageProgress existing = new LessonStageProgress();
        existing.setId(7L);
        existing.setStage("GRAMMAR");
        existing.setScore(20);
        when(stageProgressMapper.selectOne(any(QueryWrapper.class))).thenReturn(existing);

        service.gradeGrammar(USER_ID, LESSON_ID, grammarAnswers(0, 1, 2, 3));

        ArgumentCaptor<LessonStageProgress> captor = ArgumentCaptor.forClass(LessonStageProgress.class);
        verify(stageProgressMapper).updateById(captor.capture());
        assertThat(captor.getValue().getScore()).isEqualTo(100);
        verify(stageProgressMapper, never()).insert(any(LessonStageProgress.class));
    }

    // ---------- 听力 ----------

    private ListeningMaterial listeningMaterial() {
        ListeningMaterial m = new ListeningMaterial();
        m.setId(7L);
        m.setLessonId(LESSON_ID);
        m.setOptionsJson("[\"x\",\"y\",\"z\"]");
        m.setAnswerIndex(1);
        m.setExplanation("why");
        return m;
    }

    private void stubListening() {
        stubAccessible();
        when(listeningMapper.selectOne(any(QueryWrapper.class))).thenReturn(listeningMaterial());
        lenient().when(stageProgressMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
    }

    @Test
    @DisplayName("听力答对 → 100 并回正确答案与解析")
    void listeningCorrect() {
        stubListening();
        ListeningResultVO vo = service.gradeListening(USER_ID, LESSON_ID, 1);
        assertThat(vo.correct()).isTrue();
        assertThat(vo.score()).isEqualTo(100);
        assertThat(vo.correctIndex()).isEqualTo(1);
        assertThat(vo.explanation()).isEqualTo("why");
    }

    @Test
    @DisplayName("听力答错 → 0，仍回正确答案")
    void listeningWrong() {
        stubListening();
        ListeningResultVO vo = service.gradeListening(USER_ID, LESSON_ID, 0);
        assertThat(vo.correct()).isFalse();
        assertThat(vo.score()).isZero();
        assertThat(vo.correctIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("听力下标越界 → 400")
    void listeningOutOfRange400() {
        stubListening();
        assertThatThrownBy(() -> service.gradeListening(USER_ID, LESSON_ID, 3))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("课时无听力材料 → 400")
    void listeningMissing400() {
        stubAccessible();
        when(listeningMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        assertThatThrownBy(() -> service.gradeListening(USER_ID, LESSON_ID, 0))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    // ---------- 口语 ----------

    private List<SpeakingSentence> threeSentences() {
        String[] texts = {"Hello world", "Good morning", "How are you"};
        List<SpeakingSentence> list = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            SpeakingSentence s = new SpeakingSentence();
            s.setId(201L + i);
            s.setLessonId(LESSON_ID);
            s.setText(texts[i]);
            s.setSortNo(i + 1);
            list.add(s);
        }
        return list;
    }

    private void stubSpeaking() {
        stubAccessible();
        when(speakingMapper.selectList(any(QueryWrapper.class))).thenReturn(threeSentences());
        lenient().when(stageProgressMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
    }

    private SpeakingSentenceItem recItem(long id, String transcript) {
        return new SpeakingSentenceItem(id, transcript, null, null);
    }

    @Test
    @DisplayName("RECOGNITION：识别文本与原文一致 → 100")
    void speakingRecognitionExact() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("RECOGNITION", List.of(
                recItem(201L, "Hello world"),
                recItem(202L, "Good morning"),
                recItem(203L, "How are you")));

        SpeakingResultVO vo = service.gradeSpeaking(USER_ID, LESSON_ID, req);

        assertThat(vo.score()).isEqualTo(100);
        assertThat(vo.results()).allSatisfy(r -> {
            assertThat(r.score()).isEqualTo(100);
            assertThat(r.feedback()).isEqualTo("发音很流利，继续保持");
        });
    }

    @Test
    @DisplayName("RECOGNITION：近似句高分、乱读句低分、总分取均值")
    void speakingRecognitionSimilarity() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("RECOGNITION", List.of(
                recItem(201L, "Hello world!"), // 标点差异 → 归一化后一致 100
                recItem(202L, "Good mornin"),  // 少 1 字符，距离 1/11 → 91
                recItem(203L, "xxxxx")));      // 乱读 → 0

        SpeakingResultVO vo = service.gradeSpeaking(USER_ID, LESSON_ID, req);

        assertThat(vo.results()).extracting(r -> r.score()).containsExactly(100, 91, 0);
        assertThat(vo.score()).isEqualTo(64); // round((100+91+0)/3)
        assertThat(vo.results().get(2).feedback()).isEqualTo("建议再听示范并跟读一遍");
    }

    @Test
    @DisplayName("RECOGNITION：缺识别文本时回退客户端分数")
    void speakingRecognitionFallbackClientScore() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("RECOGNITION", List.of(
                recItem(201L, "Hello world"),
                new SpeakingSentenceItem(202L, null, 77, null),
                recItem(203L, "How are you")));

        SpeakingResultVO vo = service.gradeSpeaking(USER_ID, LESSON_ID, req);
        assertThat(vo.results().get(1).score()).isEqualTo(77);
    }

    @Test
    @DisplayName("RECOGNITION：识别文本与客户端分数都缺 → 400")
    void speakingRecognitionMissingBoth400() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("RECOGNITION", List.of(
                recItem(201L, "Hello world"),
                recItem(202L, "  "),
                recItem(203L, "How are you")));
        assertThatThrownBy(() -> service.gradeSpeaking(USER_ID, LESSON_ID, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("SELF：三档 60/80/95，总分取均值 78")
    void speakingSelfRatings() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("SELF", List.of(
                new SpeakingSentenceItem(201L, null, null, "NEED_PRACTICE"),
                new SpeakingSentenceItem(202L, null, null, "OK"),
                new SpeakingSentenceItem(203L, null, null, "FLUENT")));

        SpeakingResultVO vo = service.gradeSpeaking(USER_ID, LESSON_ID, req);

        assertThat(vo.results()).extracting(r -> r.score()).containsExactly(60, 80, 95);
        assertThat(vo.score()).isEqualTo(78); // round(235/3)
        assertThat(vo.results().get(0).feedback()).isEqualTo("读得不错，注意个别发音");
        assertThat(vo.results().get(2).feedback()).isEqualTo("发音很流利，继续保持");
    }

    @Test
    @DisplayName("SELF：缺自评档位 → 400")
    void speakingSelfMissingRating400() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("SELF", List.of(
                new SpeakingSentenceItem(201L, null, null, "OK"),
                new SpeakingSentenceItem(202L, null, null, null),
                new SpeakingSentenceItem(203L, null, null, "OK")));
        assertThatThrownBy(() -> service.gradeSpeaking(USER_ID, LESSON_ID, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("口语句数不符 → 400")
    void speakingCountMismatch400() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("SELF", List.of(
                new SpeakingSentenceItem(201L, null, null, "OK")));
        assertThatThrownBy(() -> service.gradeSpeaking(USER_ID, LESSON_ID, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("混入他课时句子 → 400")
    void speakingForeignSentence400() {
        stubSpeaking();
        SpeakingSubmitRequest req = new SpeakingSubmitRequest("SELF", List.of(
                new SpeakingSentenceItem(201L, null, null, "OK"),
                new SpeakingSentenceItem(202L, null, null, "OK"),
                new SpeakingSentenceItem(999L, null, null, "OK")));
        assertThatThrownBy(() -> service.gradeSpeaking(USER_ID, LESSON_ID, req))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    // ---------- 相似度纯函数 ----------

    @Test
    @DisplayName("similarityScore：一致 100、大小写标点空格归一化、空串 0、全异 0、日文兼容")
    void similarityScoreRules() {
        assertThat(QuizService.similarityScore("Hello world", "Hello world")).isEqualTo(100);
        assertThat(QuizService.similarityScore("Hello, World!", "hello world")).isEqualTo(100);
        assertThat(QuizService.similarityScore("Hello world", "")).isZero();
        assertThat(QuizService.similarityScore("aaaa", "bbbb")).isZero();
        assertThat(QuizService.similarityScore("こんにちは", "こんにちは")).isEqualTo(100);
        assertThat(QuizService.similarityScore("こんにちは", "こんにちわ")).isEqualTo(80);
    }

    // ---------- 权限与资源校验 ----------

    @Test
    @DisplayName("课时不存在 → 404")
    void lessonNotFound404() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.gradeListening(USER_ID, LESSON_ID, 0))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("课时未发布 → 404")
    void lessonUnreleased404() {
        lesson.setReleased(false);
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        assertThatThrownBy(() -> service.gradeListening(USER_ID, LESSON_ID, 0))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("未选学 → 403")
    void notEnrolled403() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(null);
        assertThatThrownBy(() -> service.gradeListening(USER_ID, LESSON_ID, 0))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("课时未解锁 → 403")
    void lockedLesson403() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(false);
        assertThatThrownBy(() -> service.gradeListening(USER_ID, LESSON_ID, 0))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }
}
