package cn.linguaverse.stats.service;

import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.learn.service.ProgressService;
import cn.linguaverse.stats.dto.StatsViews.RecommendationVO;
import cn.linguaverse.stats.dto.StatsViews.TodayPlanVO;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Task 8：今日计划规则引擎。
 * 新用户：定级 / 直接开课 / 设目标；
 * 老用户：到期复习 → 续学下一课 → 薄弱模块训练；
 * 达标后文案切换为鼓励式。每条推荐含理由与客户端直达 deeplink。
 */
@Service
public class RecommendationService {

    private static final int WEAK_THRESHOLD = 70;

    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final LessonMapper lessonMapper;
    private final WordMasteryMapper wordMasteryMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    public RecommendationService(EnrollmentService enrollmentService, ProgressService progressService,
                                 LessonMapper lessonMapper, WordMasteryMapper wordMasteryMapper,
                                 ActivityMapper activityMapper, UserMapper userMapper) {
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.lessonMapper = lessonMapper;
        this.wordMasteryMapper = wordMasteryMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
    }

    public TodayPlanVO today(long userId) {
        User user = userMapper.selectById(userId);
        LocalDate today = LocalDate.now();
        int goalXp = user.getDailyGoalXp() == null ? 30 : user.getDailyGoalXp();
        int streak = user.getStreak() == null ? 0 : user.getStreak();

        List<Activity> recent = activityMapper.selectList(
                new QueryWrapper<Activity>().eq("user_id", userId).orderByDesc("created_at"));
        int todayXp = (int) recent.stream()
                .filter(a -> a.getCreatedAt() != null && !a.getCreatedAt().isBefore(today.atStartOfDay())
                        && a.getXp() != null)
                .mapToLong(Activity::getXp).sum();
        boolean goalReached = todayXp >= goalXp;

        List<RecommendationVO> recs = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentService.listByUser(userId);

        if (enrollments.isEmpty()) {
            recs.add(new RecommendationVO("TAKE_PLACEMENT", "PLACEMENT",
                    "先做个 2 分钟定级测试",
                    "10 道小题测出适合你的级别，完成后自动开课，不必从最简单的内容开始。",
                    "开始定级", "/placement/en", 100));
            recs.add(new RecommendationVO("DIRECT_START", "LESSON",
                    "或直接从英语 L1 开始",
                    "不想测试也能直接开课，从第一节课的打招呼学起。",
                    "直接开始", "/courses/en", 90));
            recs.add(new RecommendationVO("SET_GOAL", "GOAL",
                    "设定今日小目标",
                    "每天 10 / 30 / 60 / 100 XP 四档可选，按自己的节奏盖章。",
                    "去设置", "/daily-goal", 80));
        } else {
            // 1) 到期复习（取到期词最多的语种）
            List<WordMastery> cards = wordMasteryMapper.selectList(
                    new QueryWrapper<WordMastery>().eq("user_id", userId));
            String dueLang = null;
            int dueMax = 0;
            for (Enrollment e : enrollments) {
                int due = (int) cards.stream()
                        .filter(c -> e.getLangCode().equals(c.getLangCode())
                                && !Boolean.TRUE.equals(c.getMastered())
                                && c.getDueDate() != null && !c.getDueDate().isAfter(today))
                        .count();
                if (due > dueMax) {
                    dueMax = due;
                    dueLang = e.getLangCode();
                }
            }
            if (dueMax > 0) {
                recs.add(new RecommendationVO("REVIEW_DUE", "REVIEW",
                        dueMax + " 个单词今天该复习了",
                        "按记忆曲线，到期词及时复习记得最牢，约 " + Math.max(1, dueMax / 5) + " 分钟。",
                        "开始复习", "/review?lang=" + dueLang, 100));
            }

            // 2) 续学：每个语种找第一个未完成且已解锁的课时，主修语种优先
            for (Enrollment e : enrollments) {
                List<Lesson> lessons = lessonMapper.selectList(new QueryWrapper<Lesson>()
                        .eq("lang_code", e.getLangCode()).eq("released", true).orderByAsc("seq_no"));
                for (Lesson lesson : lessons) {
                    if (progressService.isCompleted(userId, lesson.getId())) {
                        continue;
                    }
                    if (progressService.isUnlocked(userId, lesson)) {
                        boolean first = progressService.completedCount(userId, e.getLangCode()) == 0
                                && lesson.getSeqNo() <= 1;
                        String key = first ? "FIRST_LESSON" : "CONTINUE_LESSON";
                        String title = first
                                ? "开始你的第一节课" : "继续学习：" + lesson.getTitle();
                        String reason = first
                                ? "课程已就绪，完成四环节大约 10 分钟，拿下第一枚课时邮戳。"
                                : "上一节课已完成，下一节已解锁，趁热度继续。";
                        recs.add(new RecommendationVO(key, "LESSON", title, reason,
                                "进入课时", "/lessons/" + lesson.getId(), 90));
                        break;
                    }
                }
                if (recs.stream().anyMatch(r -> "LESSON".equals(r.type()))) {
                    break;
                }
            }

            // 3) 薄弱模块训练（最近 30 次练习均分低于 70）
            weakModule(recent).ifPresent(w -> {
                Lesson lesson = lessonMapper.selectById(w.lessonId());
                String lessonTitle = lesson == null ? "上一次练习" : "《" + lesson.getTitle() + "》";
                recs.add(new RecommendationVO("WEAK_" + w.type(), "PRACTICE",
                        DashboardService.moduleLabel(w.type()) + "模块可以再巩固",
                        "最近 " + w.attempts() + " 次" + DashboardService.moduleLabel(w.type())
                                + "练习平均 " + w.avgScore() + " 分，回到" + lessonTitle + "练一组。",
                        "针对性练习", "/lessons/" + w.lessonId(), 80));
            });

            // 达标鼓励：保住 streak
            if (goalReached && streak >= 2) {
                recs.add(new RecommendationVO("KEEP_STREAK", "STREAK",
                        "已连续学习 " + streak + " 天",
                        "今日目标已达成！明天再来一天就能延续连续纪录。",
                        "查看连续纪录", "/dashboard", 60));
            }

            // 兜底：已发布内容全部通关
            boolean allDone = true;
            for (Enrollment e : enrollments) {
                List<Lesson> released = lessonMapper.selectList(new QueryWrapper<Lesson>()
                        .eq("lang_code", e.getLangCode()).eq("released", true));
                long done = released.stream().filter(l -> progressService.isCompleted(userId, l.getId())).count();
                if (done < released.size()) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                recs.add(new RecommendationVO("WAIT_NEXT_LEVEL", "EXPLORE",
                        "已发布课程全部通关",
                        "L4–L6 新级别即将上线，先复习到期词保持手感。",
                        "去复习", "/review", 70));
            }
            if (enrollments.size() < 3) {
                recs.add(new RecommendationVO("EXPLORE_LANG", "EXPLORE",
                        "试试一门新语言",
                        "日语、韩语课程也已开放，同一套学习方法直接复用。",
                        "去看看", "/courses", 40));
            }
            recs.add(new RecommendationVO("ACHIEVEMENT_WALL", "ACHIEVEMENT",
                    "看看你的成就墙",
                    "每枚邮戳都记录着真实的学习时刻。",
                    "成就墙", "/achievements", 30));
        }

        List<RecommendationVO> ordered = recs.stream()
                .sorted(Comparator.comparingInt(RecommendationVO::priority).reversed())
                .limit(4)
                .toList();

        String message = goalReached
                ? "今日目标已达成（" + todayXp + "/" + goalXp + " XP）！保持节奏"
                        + (streak > 0 ? "，已连续 " + streak + " 天，" : "") + "明天记得来盖章。"
                : "再获得 " + Math.max(0, goalXp - todayXp) + " XP 就能完成今日目标，半节课就够。";

        return new TodayPlanVO(today, todayXp, goalXp, goalReached, message, ordered);
    }

    private record WeakModule(String type, int avgScore, int attempts, long lessonId) {}

    private java.util.Optional<WeakModule> weakModule(List<Activity> recent) {
        WeakModule weakest = null;
        for (String type : List.of("GRAMMAR", "LISTENING", "SPEAKING")) {
            List<Activity> rows = recent.stream()
                    .filter(a -> type.equals(a.getType()) && a.getScore() != null && a.getRefId() != null)
                    .limit(30).toList();
            if (rows.isEmpty()) {
                continue;
            }
            int avg = (int) Math.round(rows.stream().mapToInt(Activity::getScore).average().orElse(0));
            if (avg < WEAK_THRESHOLD
                    && (weakest == null || avg < weakest.avgScore())) {
                weakest = new WeakModule(type, avg, rows.size(), rows.get(0).getRefId());
            }
        }
        return java.util.Optional.ofNullable(weakest);
    }
}
