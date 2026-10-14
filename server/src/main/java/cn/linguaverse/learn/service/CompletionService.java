package cn.linguaverse.learn.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.LessonVocab;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.learn.dto.CompletionViews.*;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Task 7：课时结算。
 * 同一用户同一课时唯一键 uk_lc_user_lesson 保证幂等：重复请求返回 alreadyCompleted=true，
 * 不重复写 completion/activity、不重复发 XP、不重复累计 streak、不重复颁发成就。
 */
@Service
public class CompletionService {

    private static final List<String> FOUR_STAGES = List.of("VOCAB", "GRAMMAR", "LISTENING", "SPEAKING");
    private static final int STAGE_XP = 5;
    private static final int WORD_XP = 2;
    private static final int MAX_ACCURACY_BONUS = 20;

    private final LessonMapper lessonMapper;
    private final LessonVocabMapper vocabMapper;
    private final LessonCompletionMapper completionMapper;
    private final LessonStageProgressMapper stageProgressMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final AchievementService achievementService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper;

    public CompletionService(LessonMapper lessonMapper, LessonVocabMapper vocabMapper,
                             LessonCompletionMapper completionMapper,
                             LessonStageProgressMapper stageProgressMapper,
                             ActivityMapper activityMapper, UserMapper userMapper,
                             AchievementService achievementService,
                             EnrollmentService enrollmentService, ProgressService progressService,
                             ObjectMapper objectMapper) {
        this.lessonMapper = lessonMapper;
        this.vocabMapper = vocabMapper;
        this.completionMapper = completionMapper;
        this.stageProgressMapper = stageProgressMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
        this.achievementService = achievementService;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CompleteVO complete(long userId, long lessonId, int durationSec) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || !Boolean.TRUE.equals(lesson.getReleased())) {
            throw BizException.notFound("课时不存在");
        }
        if (enrollmentService.get(userId, lesson.getLangCode()) == null) {
            throw BizException.forbidden("请先加入该语种的学习");
        }
        if (!progressService.isUnlocked(userId, lesson)) {
            throw BizException.forbidden("请先完成上一节课再解锁本课");
        }

        // 幂等：已结算直接回放，不重发任何奖励
        LessonCompletion existing = completionMapper.selectOne(new QueryWrapper<LessonCompletion>()
                .eq("user_id", userId).eq("lesson_id", lessonId));
        if (existing != null) {
            return replay(existing, lesson);
        }

        // 四环节成绩必须齐全
        List<LessonStageProgress> stages = stageProgressMapper.selectList(
                new QueryWrapper<LessonStageProgress>()
                        .eq("user_id", userId).eq("lesson_id", lessonId));
        Map<String, Integer> scoreByStage = new java.util.HashMap<>();
        for (LessonStageProgress p : stages) {
            if (p.getScore() != null) {
                scoreByStage.put(p.getStage(), p.getScore());
            }
        }
        List<String> missing = FOUR_STAGES.stream().filter(s -> !scoreByStage.containsKey(s)).toList();
        if (!missing.isEmpty()) {
            throw BizException.badRequest("还有环节未完成，无法结算: " + String.join("/", missing));
        }
        int accuracy = (int) Math.round(
                FOUR_STAGES.stream().mapToInt(scoreByStage::get).average().orElse(0));

        int wordsCount = Math.toIntExact(vocabMapper.selectCount(
                new QueryWrapper<LessonVocab>().eq("lesson_id", lessonId)));
        int baseXp = wordsCount * WORD_XP + FOUR_STAGES.size() * STAGE_XP;
        int bonusXp = Math.round(accuracy / 100f * MAX_ACCURACY_BONUS);
        int totalXp = baseXp + bonusXp;

        LocalDateTime now = LocalDateTime.now();
        LessonCompletion completion = new LessonCompletion();
        completion.setUserId(userId);
        completion.setLessonId(lessonId);
        completion.setXp(totalXp);
        completion.setAccuracy(accuracy);
        completion.setWordsCount(wordsCount);
        completion.setDurationSec(durationSec);
        completion.setCompletedAt(now);
        try {
            completionMapper.insert(completion);
        } catch (DuplicateKeyException e) {
            // 并发重复结算：回查已有记录，不发奖
            LessonCompletion raced = completionMapper.selectOne(new QueryWrapper<LessonCompletion>()
                    .eq("user_id", userId).eq("lesson_id", lessonId));
            return replay(raced, lesson);
        }

        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setType("LESSON_DONE");
        activity.setRefId(lessonId);
        activity.setXp(totalXp);
        activity.setScore(accuracy);
        try {
            activity.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "baseXp", baseXp,
                    "bonusXp", bonusXp,
                    "wordsCount", wordsCount,
                    "durationSec", durationSec,
                    "stageScores", scoreByStage)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("结算 payload 序列化失败", e);
        }
        activity.setCreatedAt(now);
        activityMapper.insert(activity);

        int streak = advanceStreak(userId);
        List<String> newAchievements = achievementService.evaluateOnLessonComplete(userId, streak, accuracy);

        return new CompleteVO(lessonId, false, new XpDetail(baseXp, bonusXp, totalXp), accuracy,
                wordsCount, durationSec, streak, now, nextLesson(lesson), newAchievements);
    }

    /** 连续学习：昨天学过 → +1；今天已计 → 不变；更早或从未 → 重置为 1。 */
    private int advanceStreak(long userId) {
        User user = userMapper.selectById(userId);
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastStudyDate();
        int streak;
        if (today.equals(last)) {
            streak = user.getStreak() == null ? 1 : user.getStreak();
        } else if (last != null && last.equals(today.minusDays(1))) {
            streak = (user.getStreak() == null ? 0 : user.getStreak()) + 1;
        } else {
            streak = 1;
        }
        user.setStreak(streak);
        user.setLastStudyDate(today);
        userMapper.updateById(user);
        return streak;
    }

    private int currentStreak(long userId) {
        User user = userMapper.selectById(userId);
        return user.getStreak() == null ? 0 : user.getStreak();
    }

    /** 幂等回放：从 LESSON_DONE 流水还原 XP 明细，newAchievements 恒为空。 */
    private CompleteVO replay(LessonCompletion c, Lesson lesson) {
        int base = c.getXp();
        int bonus = 0;
        Activity activity = activityMapper.selectOne(new QueryWrapper<Activity>()
                .eq("user_id", c.getUserId()).eq("ref_id", c.getLessonId())
                .eq("type", "LESSON_DONE").orderByDesc("id").last("LIMIT 1"));
        if (activity != null && activity.getPayloadJson() != null) {
            try {
                var node = objectMapper.readTree(activity.getPayloadJson());
                base = node.path("baseXp").asInt(c.getXp());
                bonus = node.path("bonusXp").asInt(0);
            } catch (JsonProcessingException ignored) {
                // 流水损坏时回退为总额
            }
        }
        return new CompleteVO(c.getLessonId(), true, new XpDetail(base, bonus, c.getXp()),
                c.getAccuracy(), c.getWordsCount(), c.getDurationSec(), currentStreak(c.getUserId()),
                c.getCompletedAt(), nextLesson(lesson), List.of());
    }

    private NextLessonVO nextLesson(Lesson current) {
        Lesson next = lessonMapper.selectOne(new QueryWrapper<Lesson>()
                .eq("lang_code", current.getLangCode())
                .eq("seq_no", current.getSeqNo() + 1)
                .eq("released", true));
        if (next == null) {
            return null;
        }
        return new NextLessonVO(next.getId(), next.getSeqNo(), next.getSlug(), next.getTitle());
    }

    private CompleteVO toVO(LessonCompletion c, boolean alreadyCompleted, int base, int bonus,
                            int total, int streak, List<String> newAchievements, NextLessonVO next) {
        Lesson lesson = lessonMapper.selectById(c.getLessonId());
        XpDetail xp;
        if (alreadyCompleted) {
            xp = new XpDetail(c.getXp(), 0, c.getXp());
        } else {
            xp = new XpDetail(base, bonus, total);
        }
        return new CompleteVO(c.getLessonId(), alreadyCompleted, xp, c.getAccuracy(),
                c.getWordsCount(), c.getDurationSec(), streak, c.getCompletedAt(), next,
                newAchievements);
    }
}
