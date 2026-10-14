package cn.linguaverse.achieve.service;

import cn.linguaverse.achieve.AchievementCatalog;
import cn.linguaverse.achieve.dto.AchievementVO;
import cn.linguaverse.achieve.entity.AchievementUnlock;
import cn.linguaverse.achieve.mapper.AchievementUnlockMapper;
import cn.linguaverse.community.entity.Post;
import cn.linguaverse.community.mapper.PostMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.EnrollmentMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** GET /achievements：8 枚静态目录 + 当前用户解锁状态与实时进度。 */
@Service
public class AchievementQueryService {

    private final AchievementUnlockMapper unlockMapper;
    private final LessonCompletionMapper completionMapper;
    private final WordMasteryMapper wordMasteryMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public AchievementQueryService(AchievementUnlockMapper unlockMapper,
                                   LessonCompletionMapper completionMapper,
                                   WordMasteryMapper wordMasteryMapper,
                                   EnrollmentMapper enrollmentMapper,
                                   PostMapper postMapper,
                                   UserMapper userMapper) {
        this.unlockMapper = unlockMapper;
        this.completionMapper = completionMapper;
        this.wordMasteryMapper = wordMasteryMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    public List<AchievementVO> list(long userId) {
        Map<String, LocalDateTime> unlockedAt = unlockMapper.selectList(
                        new QueryWrapper<AchievementUnlock>().eq("user_id", userId))
                .stream().collect(Collectors.toMap(AchievementUnlock::getCode,
                        AchievementUnlock::getUnlockedAt, (a, b) -> a));

        long lessons = completionMapper.selectCount(
                new QueryWrapper<LessonCompletion>().eq("user_id", userId));
        long perfect = completionMapper.selectCount(
                new QueryWrapper<LessonCompletion>().eq("user_id", userId).eq("accuracy", 100));
        long mastered = wordMasteryMapper.selectCount(
                new QueryWrapper<WordMastery>().eq("user_id", userId).eq("mastered", true));
        long langs = enrollmentMapper.selectCount(
                new QueryWrapper<Enrollment>().eq("user_id", userId));
        long posts = postMapper.selectCount(
                new QueryWrapper<Post>().eq("author_id", userId));
        User user = userMapper.selectById(userId);
        long streak = user == null || user.getStreak() == null ? 0 : user.getStreak();

        return Arrays.stream(AchievementCatalog.values()).map(c -> {
            LocalDateTime at = unlockedAt.get(c.code());
            long current = switch (c) {
                case FIRST_STEP -> lessons;
                case STREAK_3 -> streak;
                case STREAK_7 -> streak;
                case LESSONS_10 -> lessons;
                case WORDS_50 -> mastered;
                case PERFECT -> perfect;
                case POLYGLOT -> langs;
                case SOCIAL -> posts;
            };
            long target = switch (c) {
                case FIRST_STEP, PERFECT, SOCIAL -> 1;
                case STREAK_3 -> 3;
                case STREAK_7 -> 7;
                case LESSONS_10 -> 10;
                case WORDS_50 -> 50;
                case POLYGLOT -> 3;
            };
            boolean isUnlocked = at != null;
            return new AchievementVO(c.code(), c.nameZh(), c.descZh(), c.icon(),
                    isUnlocked, at,
                    Math.min(current, target), target);
        }).toList();
    }
}
