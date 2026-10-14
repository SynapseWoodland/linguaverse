package cn.linguaverse.learn.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.LessonVocab;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.learn.dto.CompletionViews.CompleteVO;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 课时结算：XP 公式、accuracy 取整、Streak 推进、幂等回放、并发兜底、前置校验。 */
@ExtendWith(MockitoExtension.class)
class CompletionServiceTest {

    private static final long USER_ID = 9L;
    private static final long LESSON_ID = 1L;

    @Mock
    LessonMapper lessonMapper;
    @Mock
    LessonVocabMapper vocabMapper;
    @Mock
    LessonCompletionMapper completionMapper;
    @Mock
    LessonStageProgressMapper stageProgressMapper;
    @Mock
    ActivityMapper activityMapper;
    @Mock
    UserMapper userMapper;
    @Mock
    AchievementService achievementService;
    @Mock
    EnrollmentService enrollmentService;
    @Mock
    ProgressService progressService;

    CompletionService service;
    Lesson lesson;

    @BeforeEach
    void setUp() {
        service = new CompletionService(lessonMapper, vocabMapper, completionMapper,
                stageProgressMapper, activityMapper, userMapper, achievementService,
                enrollmentService, progressService, new ObjectMapper());
        lesson = new Lesson();
        lesson.setId(LESSON_ID);
        lesson.setLangCode("en");
        lesson.setSeqNo(1);
        lesson.setSlug("en-l1");
        lesson.setTitle("Greetings");
        lesson.setReleased(true);
    }

    private List<LessonStageProgress> stages(int vocab, int grammar, int listening, int speaking) {
        int[] scores = {vocab, grammar, listening, speaking};
        String[] names = {"VOCAB", "GRAMMAR", "LISTENING", "SPEAKING"};
        return java.util.stream.IntStream.range(0, 4).mapToObj(i -> {
            LessonStageProgress p = new LessonStageProgress();
            p.setUserId(USER_ID);
            p.setLessonId(LESSON_ID);
            p.setStage(names[i]);
            p.setScore(scores[i]);
            return p;
        }).toList();
    }

    private User userWith(Integer streak, LocalDate lastStudyDate) {
        User u = new User();
        u.setId(USER_ID);
        u.setStreak(streak);
        u.setLastStudyDate(lastStudyDate);
        return u;
    }

    /** 可结算课时的完整前置：已选学、已解锁、四环节齐全、8 词、下一课存在。 */
    private void stubGradeable(User user, List<LessonStageProgress> stageRows) {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);
        when(completionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(stageProgressMapper.selectList(any(QueryWrapper.class))).thenReturn(stageRows);
        when(vocabMapper.selectCount(any(QueryWrapper.class))).thenReturn(8L);
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(achievementService.evaluateOnLessonComplete(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of("first_step"));
        Lesson next = new Lesson();
        next.setId(2L);
        next.setSeqNo(2);
        next.setSlug("en-l2");
        next.setTitle("Numbers");
        when(lessonMapper.selectOne(any(QueryWrapper.class))).thenReturn(next);
    }

    @Test
    @DisplayName("结算：accuracy 92.5→93，XP=36+19=55，流水/成就/下一课齐全")
    void completeHappyPath() {
        User user = userWith(2, LocalDate.now().minusDays(1));
        stubGradeable(user, stages(90, 95, 85, 100));

        CompleteVO vo = service.complete(USER_ID, LESSON_ID, 300);

        assertThat(vo.alreadyCompleted()).isFalse();
        assertThat(vo.accuracy()).isEqualTo(93);
        assertThat(vo.xp().base()).isEqualTo(36);  // 8 词×2 + 4 环节×5
        assertThat(vo.xp().bonus()).isEqualTo(19); // round(93%×20)
        assertThat(vo.xp().total()).isEqualTo(55);
        assertThat(vo.wordsCount()).isEqualTo(8);
        assertThat(vo.durationSec()).isEqualTo(300);
        assertThat(vo.streak()).isEqualTo(3);
        assertThat(vo.completedAt()).isNotNull();
        assertThat(vo.newAchievements()).containsExactly("first_step");
        assertThat(vo.nextLesson().id()).isEqualTo(2L);
        assertThat(vo.nextLesson().seqNo()).isEqualTo(2);

        ArgumentCaptor<LessonCompletion> completionCaptor = ArgumentCaptor.forClass(LessonCompletion.class);
        verify(completionMapper).insert(completionCaptor.capture());
        assertThat(completionCaptor.getValue().getXp()).isEqualTo(55);
        assertThat(completionCaptor.getValue().getAccuracy()).isEqualTo(93);

        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).insert(activityCaptor.capture());
        Activity a = activityCaptor.getValue();
        assertThat(a.getType()).isEqualTo("LESSON_DONE");
        assertThat(a.getXp()).isEqualTo(55);
        assertThat(a.getScore()).isEqualTo(93);
        assertThat(a.getPayloadJson()).contains("\"baseXp\":36").contains("\"bonusXp\":19");

        verify(achievementService).evaluateOnLessonComplete(USER_ID, 3, 93);
    }

    @Test
    @DisplayName("满分课：accuracy=100，bonus 拿满 20，总 XP=56")
    void completePerfectScore() {
        User user = userWith(0, null);
        stubGradeable(user, stages(100, 100, 100, 100));

        CompleteVO vo = service.complete(USER_ID, LESSON_ID, 60);

        assertThat(vo.accuracy()).isEqualTo(100);
        assertThat(vo.xp().base()).isEqualTo(36);
        assertThat(vo.xp().bonus()).isEqualTo(20);
        assertThat(vo.xp().total()).isEqualTo(56);
    }

    @Test
    @DisplayName("Streak：今天已学过 → 不变")
    void streakSameDayUnchanged() {
        User user = userWith(5, LocalDate.now());
        stubGradeable(user, stages(80, 80, 80, 80));

        CompleteVO vo = service.complete(USER_ID, LESSON_ID, 60);

        assertThat(vo.streak()).isEqualTo(5);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStreak()).isEqualTo(5);
        assertThat(captor.getValue().getLastStudyDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Streak：昨天学过 → +1")
    void streakYesterdayPlusOne() {
        User user = userWith(2, LocalDate.now().minusDays(1));
        stubGradeable(user, stages(80, 80, 80, 80));

        assertThat(service.complete(USER_ID, LESSON_ID, 60).streak()).isEqualTo(3);
    }

    @Test
    @DisplayName("Streak：断签（3 天前）→ 重置为 1")
    void streakBrokenResetsToOne() {
        User user = userWith(9, LocalDate.now().minusDays(3));
        stubGradeable(user, stages(80, 80, 80, 80));

        assertThat(service.complete(USER_ID, LESSON_ID, 60).streak()).isEqualTo(1);
    }

    @Test
    @DisplayName("Streak：从未学过 → 1")
    void streakFirstTimeIsOne() {
        User user = userWith(null, null);
        stubGradeable(user, stages(80, 80, 80, 80));

        assertThat(service.complete(USER_ID, LESSON_ID, 60).streak()).isEqualTo(1);
    }

    @Test
    @DisplayName("缺环节 → 400 并列明缺失项")
    void missingStage400() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);
        when(completionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(stageProgressMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(stages(80, 80, 80, 80).subList(0, 3)); // 缺 SPEAKING

        assertThatThrownBy(() -> service.complete(USER_ID, LESSON_ID, 60))
                .isInstanceOfSatisfying(BizException.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(400);
                    assertThat(e.getMessage()).contains("SPEAKING");
                });
    }

    @Test
    @DisplayName("重复结算 → 幂等回放，XP 从流水还原，不重发奖/成就")
    void repeatCompletionReplays() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);

        LessonCompletion done = new LessonCompletion();
        done.setUserId(USER_ID);
        done.setLessonId(LESSON_ID);
        done.setXp(55);
        done.setAccuracy(93);
        done.setWordsCount(8);
        done.setDurationSec(300);
        done.setCompletedAt(LocalDateTime.now().minusDays(1));
        when(completionMapper.selectOne(any(QueryWrapper.class))).thenReturn(done);

        Activity ledger = new Activity();
        ledger.setPayloadJson("{\"baseXp\":36,\"bonusXp\":19}");
        when(activityMapper.selectOne(any(QueryWrapper.class))).thenReturn(ledger);
        when(userMapper.selectById(USER_ID)).thenReturn(userWith(7, LocalDate.now()));
        when(lessonMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        CompleteVO vo = service.complete(USER_ID, LESSON_ID, 999);

        assertThat(vo.alreadyCompleted()).isTrue();
        assertThat(vo.xp().base()).isEqualTo(36);
        assertThat(vo.xp().bonus()).isEqualTo(19);
        assertThat(vo.xp().total()).isEqualTo(55);
        assertThat(vo.durationSec()).isEqualTo(300); // 不被新请求覆盖
        assertThat(vo.streak()).isEqualTo(7);
        assertThat(vo.newAchievements()).isEmpty();
        assertThat(vo.nextLesson()).isNull();

        verify(completionMapper, never()).insert(any(LessonCompletion.class));
        verify(activityMapper, never()).insert(any(Activity.class));
        verify(achievementService, never()).evaluateOnLessonComplete(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("回放时流水缺失 → XP 明细回退为总额")
    void replayWithoutLedger() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);

        LessonCompletion done = new LessonCompletion();
        done.setUserId(USER_ID);
        done.setLessonId(LESSON_ID);
        done.setXp(55);
        done.setAccuracy(93);
        done.setWordsCount(8);
        done.setDurationSec(300);
        done.setCompletedAt(LocalDateTime.now());
        when(completionMapper.selectOne(any(QueryWrapper.class))).thenReturn(done);
        when(activityMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(userMapper.selectById(USER_ID)).thenReturn(userWith(1, LocalDate.now()));
        when(lessonMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        CompleteVO vo = service.complete(USER_ID, LESSON_ID, 60);

        assertThat(vo.xp().base()).isEqualTo(55);
        assertThat(vo.xp().bonus()).isZero();
        assertThat(vo.xp().total()).isEqualTo(55);
    }

    @Test
    @DisplayName("并发重复结算（唯一键冲突）→ 回放已有记录，不抛异常")
    void concurrentDuplicateKeyReplays() {
        User user = userWith(1, LocalDate.now());
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);

        LessonCompletion raced = new LessonCompletion();
        raced.setUserId(USER_ID);
        raced.setLessonId(LESSON_ID);
        raced.setXp(55);
        raced.setAccuracy(93);
        raced.setWordsCount(8);
        raced.setDurationSec(300);
        raced.setCompletedAt(LocalDateTime.now());
        // 第一次查无记录，insert 撞唯一键后回查命中
        when(completionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null, raced);
        when(stageProgressMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(stages(90, 95, 85, 100));
        when(vocabMapper.selectCount(any(QueryWrapper.class))).thenReturn(8L);
        when(completionMapper.insert(any(LessonCompletion.class)))
                .thenThrow(new DuplicateKeyException("uk_lc_user_lesson"));
        when(activityMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(lessonMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        CompleteVO vo = service.complete(USER_ID, LESSON_ID, 300);

        assertThat(vo.alreadyCompleted()).isTrue();
        assertThat(vo.xp().total()).isEqualTo(55);
        assertThat(vo.newAchievements()).isEmpty();
        verify(achievementService, never()).evaluateOnLessonComplete(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("课时不存在 → 404")
    void lessonNotFound404() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.complete(USER_ID, LESSON_ID, 60))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("未选学 → 403")
    void notEnrolled403() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(null);
        assertThatThrownBy(() -> service.complete(USER_ID, LESSON_ID, 60))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("课时未解锁 → 403")
    void lockedLesson403() {
        when(lessonMapper.selectById(LESSON_ID)).thenReturn(lesson);
        when(enrollmentService.get(USER_ID, "en")).thenReturn(mock(Enrollment.class));
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(false);
        assertThatThrownBy(() -> service.complete(USER_ID, LESSON_ID, 60))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }
}
