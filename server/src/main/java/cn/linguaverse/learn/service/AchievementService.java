package cn.linguaverse.learn.service;

import cn.linguaverse.achieve.AchievementCatalog;
import cn.linguaverse.achieve.entity.AchievementUnlock;
import cn.linguaverse.achieve.mapper.AchievementUnlockMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.EnrollmentMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 成就评估（各触发点幂等，uk_au_user_code 唯一键兜底）：
 * Task 7 结算路径 5 枚；Task 9 首帖 social；Task 10 补齐 words_50（SRS 掌握）与 polyglot（加入 3 语种）。
 */
@Service
public class AchievementService {

    private final AchievementUnlockMapper unlockMapper;
    private final LessonCompletionMapper completionMapper;
    private final WordMasteryMapper wordMasteryMapper;
    private final EnrollmentMapper enrollmentMapper;

    public AchievementService(AchievementUnlockMapper unlockMapper,
                              LessonCompletionMapper completionMapper,
                              WordMasteryMapper wordMasteryMapper,
                              EnrollmentMapper enrollmentMapper) {
        this.unlockMapper = unlockMapper;
        this.completionMapper = completionMapper;
        this.wordMasteryMapper = wordMasteryMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    /** 课时结算后评估，返回本次新解锁的徽章 code（幂等，已拥有的不重复返回）。 */
    public List<String> evaluateOnLessonComplete(long userId, int streak, int accuracy) {
        long completedCount = completionMapper.selectCount(
                new QueryWrapper<LessonCompletion>().eq("user_id", userId));

        List<String> candidates = new ArrayList<>();
        if (completedCount >= 1) {
            candidates.add(AchievementCatalog.FIRST_STEP.code());
        }
        if (streak >= 3) {
            candidates.add(AchievementCatalog.STREAK_3.code());
        }
        if (streak >= 7) {
            candidates.add(AchievementCatalog.STREAK_7.code());
        }
        if (completedCount >= 10) {
            candidates.add(AchievementCatalog.LESSONS_10.code());
        }
        if (accuracy >= 100) {
            candidates.add(AchievementCatalog.PERFECT.code());
        }

        return unlockAll(userId, candidates);
    }

    /** 发布第一条帖子时解锁 social（幂等），返回徽章 code 或 null。Task 9 触发点。 */
    public String evaluateOnFirstPost(long userId) {
        List<String> unlocked = unlockAll(userId, List.of(AchievementCatalog.SOCIAL.code()));
        return unlocked.isEmpty() ? null : unlocked.get(0);
    }

    /** 单词新掌握时评估 words_50（累计 mastered=true ≥ 50），返回徽章 code 或 null。Task 10。 */
    public String evaluateOnWordMastered(long userId) {
        long mastered = wordMasteryMapper.selectCount(new QueryWrapper<WordMastery>()
                .eq("user_id", userId).eq("mastered", true));
        if (mastered >= 50) {
            List<String> unlocked = unlockAll(userId, List.of(AchievementCatalog.WORDS_50.code()));
            return unlocked.isEmpty() ? null : unlocked.get(0);
        }
        return null;
    }

    /** 新加入语种后评估 polyglot（加入 3 个语种），返回徽章 code 或 null。Task 10。 */
    public String evaluateOnEnrollment(long userId) {
        long langs = enrollmentMapper.selectCount(new QueryWrapper<Enrollment>()
                .eq("user_id", userId));
        if (langs >= 3) {
            List<String> unlocked = unlockAll(userId, List.of(AchievementCatalog.POLYGLOT.code()));
            return unlocked.isEmpty() ? null : unlocked.get(0);
        }
        return null;
    }

    private List<String> unlockAll(long userId, List<String> codes) {
        List<String> newlyUnlocked = new ArrayList<>();
        for (String code : codes) {
            Long exists = unlockMapper.selectCount(new QueryWrapper<AchievementUnlock>()
                    .eq("user_id", userId).eq("code", code));
            if (exists != null && exists > 0) {
                continue;
            }
            AchievementUnlock unlock = new AchievementUnlock();
            unlock.setUserId(userId);
            unlock.setCode(code);
            unlock.setUnlockedAt(LocalDateTime.now());
            unlockMapper.insert(unlock);
            newlyUnlocked.add(code);
        }
        return newlyUnlocked;
    }
}
