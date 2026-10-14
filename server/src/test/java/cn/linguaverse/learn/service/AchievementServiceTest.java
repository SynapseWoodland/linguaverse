package cn.linguaverse.learn.service;

import cn.linguaverse.achieve.entity.AchievementUnlock;
import cn.linguaverse.achieve.mapper.AchievementUnlockMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.EnrollmentMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Task 10（TR-10.2）：8 枚徽章评估器逐条件边界触发 + 重复动作不重发（uk_au_user_code 幂等）。 */
@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    private static final long USER_ID = 9L;

    @Mock
    AchievementUnlockMapper unlockMapper;
    @Mock
    LessonCompletionMapper completionMapper;
    @Mock
    WordMasteryMapper wordMasteryMapper;
    @Mock
    EnrollmentMapper enrollmentMapper;

    AchievementService service;

    @BeforeEach
    void setUp() {
        service = new AchievementService(unlockMapper, completionMapper,
                wordMasteryMapper, enrollmentMapper);
    }

    /** 尚无任何解锁记录：全部候选都会真正 insert。 */
    private void noExistingUnlocks() {
        when(unlockMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
    }

    private List<String> insertedCodes(int expectedCount) {
        ArgumentCaptor<AchievementUnlock> captor = ArgumentCaptor.forClass(AchievementUnlock.class);
        verify(unlockMapper, times(expectedCount)).insert(captor.capture());
        return captor.getAllValues().stream().map(AchievementUnlock::getCode).toList();
    }

    // ---------- evaluateOnLessonComplete：5 枚 ----------

    @Test
    @DisplayName("结算：完成 1 课/streak1/非满分 → 仅 first_step")
    void lessonCompleteMinimalTriggersOnlyFirstStep() {
        when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        noExistingUnlocks();

        List<String> unlocked = service.evaluateOnLessonComplete(USER_ID, 1, 99);

        assertThat(unlocked).containsExactly("first_step");
        assertThat(insertedCodes(1)).containsExactly("first_step");
    }

    @Test
    @DisplayName("结算边界：streak=3 触发 streak_3 但不触发 streak_7；accuracy=99 不触发 perfect")
    void lessonCompleteStreak3Boundary() {
        when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(3L);
        noExistingUnlocks();

        List<String> unlocked = service.evaluateOnLessonComplete(USER_ID, 3, 99);

        assertThat(unlocked).containsExactly("first_step", "streak_3");
    }

    @Test
    @DisplayName("结算边界：streak=7 且完成 9 课 → first_step/streak_3/streak_7，不含 lessons_10")
    void lessonCompleteStreak7WithoutLessons10() {
        when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(9L);
        noExistingUnlocks();

        List<String> unlocked = service.evaluateOnLessonComplete(USER_ID, 7, 99);

        assertThat(unlocked).containsExactly("first_step", "streak_3", "streak_7");
    }

    @Test
    @DisplayName("结算：完成 10 课/streak7/满分 → 一次集齐 5 枚，顺序固定")
    void lessonCompleteAllFive() {
        when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(10L);
        noExistingUnlocks();

        List<String> unlocked = service.evaluateOnLessonComplete(USER_ID, 7, 100);

        assertThat(unlocked).containsExactly(
                "first_step", "streak_3", "streak_7", "lessons_10", "perfect");
        assertThat(insertedCodes(5)).containsExactly(
                "first_step", "streak_3", "streak_7", "lessons_10", "perfect");

        ArgumentCaptor<AchievementUnlock> captor = ArgumentCaptor.forClass(AchievementUnlock.class);
        verify(unlockMapper, times(5)).insert(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(u -> {
            assertThat(u.getUserId()).isEqualTo(USER_ID);
            assertThat(u.getUnlockedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("结算幂等：同一动作重复评估 → 第二次不再返回、不再 insert")
    void lessonCompleteIdempotentOnRepeat() {
        when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(10L);
        // 首轮 5 次查询无记录，第二轮 5 次均已有记录
        when(unlockMapper.selectCount(any(QueryWrapper.class)))
                .thenReturn(0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L, 1L);

        List<String> first = service.evaluateOnLessonComplete(USER_ID, 7, 100);
        List<String> second = service.evaluateOnLessonComplete(USER_ID, 7, 100);

        assertThat(first).hasSize(5);
        assertThat(second).isEmpty();
        verify(unlockMapper, times(5)).insert(any(AchievementUnlock.class));
    }

    // ---------- evaluateOnFirstPost：social ----------

    @Test
    @DisplayName("首帖：未拥有 → 解锁 social 并返回 code")
    void firstPostUnlocksSocial() {
        noExistingUnlocks();

        String code = service.evaluateOnFirstPost(USER_ID);

        assertThat(code).isEqualTo("social");
        assertThat(insertedCodes(1)).containsExactly("social");
    }

    @Test
    @DisplayName("首帖幂等：已拥有 → 返回 null 且不 insert")
    void firstPostAlreadyOwned() {
        when(unlockMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        assertThat(service.evaluateOnFirstPost(USER_ID)).isNull();
        verify(unlockMapper, never()).insert(any(AchievementUnlock.class));
    }

    // ---------- evaluateOnWordMastered：words_50 ----------

    @Test
    @DisplayName("词汇边界：mastered=49 → 不解锁、不查解锁记录")
    void wordMasteredJustBelowThreshold() {
        when(wordMasteryMapper.selectCount(any(QueryWrapper.class))).thenReturn(49L);

        assertThat(service.evaluateOnWordMastered(USER_ID)).isNull();
        verify(unlockMapper, never()).insert(any(AchievementUnlock.class));
    }

    @Test
    @DisplayName("词汇边界：mastered=50 → 解锁 words_50")
    void wordMasteredAtThreshold() {
        when(wordMasteryMapper.selectCount(any(QueryWrapper.class))).thenReturn(50L);
        noExistingUnlocks();

        assertThat(service.evaluateOnWordMastered(USER_ID)).isEqualTo("words_50");
        assertThat(insertedCodes(1)).containsExactly("words_50");
    }

    @Test
    @DisplayName("词汇幂等：已达 50 但已拥有 → 返回 null 且不 insert")
    void wordMasteredAlreadyOwned() {
        when(wordMasteryMapper.selectCount(any(QueryWrapper.class))).thenReturn(50L);
        when(unlockMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        assertThat(service.evaluateOnWordMastered(USER_ID)).isNull();
        verify(unlockMapper, never()).insert(any(AchievementUnlock.class));
    }

    // ---------- evaluateOnEnrollment：polyglot ----------

    @Test
    @DisplayName("语种边界：2 语种 → 不解锁")
    void enrollmentTwoLangs() {
        when(enrollmentMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);

        assertThat(service.evaluateOnEnrollment(USER_ID)).isNull();
        verify(unlockMapper, never()).insert(any(AchievementUnlock.class));
    }

    @Test
    @DisplayName("语种边界：3 语种 → 解锁 polyglot")
    void enrollmentThreeLangs() {
        when(enrollmentMapper.selectCount(any(QueryWrapper.class))).thenReturn(3L);
        noExistingUnlocks();

        assertThat(service.evaluateOnEnrollment(USER_ID)).isEqualTo("polyglot");
        assertThat(insertedCodes(1)).containsExactly("polyglot");
    }

    @Test
    @DisplayName("语种幂等：已拥有 polyglot → 返回 null 且不 insert")
    void enrollmentAlreadyOwned() {
        when(enrollmentMapper.selectCount(any(QueryWrapper.class))).thenReturn(3L);
        when(unlockMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        assertThat(service.evaluateOnEnrollment(USER_ID)).isNull();
        verify(unlockMapper, never()).insert(any(AchievementUnlock.class));
    }
}
