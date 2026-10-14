package cn.linguaverse.stats.service;

import cn.linguaverse.course.entity.Language;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.stats.dto.StatsViews.*;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Task 8：仪表盘聚合。6 类指标全部以服务端记录为准：
 * XP/等级、Streak、今日分钟与目标、各语种完成百分比、四模块正确率、词汇分布；
 * 另附近 7 天 XP 柱。
 */
@Service
public class DashboardService {

    public static final int XP_PER_LEVEL = 500;
    private static final int RECENT_MODULE_LIMIT = 30;

    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final LessonCompletionMapper completionMapper;
    private final WordMasteryMapper wordMasteryMapper;
    private final LessonMapper lessonMapper;
    private final LanguageMapper languageMapper;
    private final EnrollmentService enrollmentService;

    public DashboardService(UserMapper userMapper, ActivityMapper activityMapper,
                            LessonCompletionMapper completionMapper, WordMasteryMapper wordMasteryMapper,
                            LessonMapper lessonMapper, LanguageMapper languageMapper,
                            EnrollmentService enrollmentService) {
        this.userMapper = userMapper;
        this.activityMapper = activityMapper;
        this.completionMapper = completionMapper;
        this.wordMasteryMapper = wordMasteryMapper;
        this.lessonMapper = lessonMapper;
        this.languageMapper = languageMapper;
        this.enrollmentService = enrollmentService;
    }

    public DashboardVO dashboard(long userId) {
        User user = userMapper.selectById(userId);
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        List<Activity> all = activityMapper.selectList(
                new QueryWrapper<Activity>().eq("user_id", userId).orderByDesc("created_at"));
        long totalXp = all.stream().filter(a -> a.getXp() != null).mapToLong(Activity::getXp).sum();

        // 今日
        int todayXp = (int) all.stream()
                .filter(a -> a.getCreatedAt() != null && !a.getCreatedAt().isBefore(todayStart)
                        && a.getXp() != null)
                .mapToLong(Activity::getXp).sum();
        List<LessonCompletion> todayCompletions = completionMapper.selectList(
                new QueryWrapper<LessonCompletion>().eq("user_id", userId)
                        .ge("completed_at", todayStart));
        int todayMinutes = (int) Math.round(todayCompletions.stream()
                .mapToInt(c -> c.getDurationSec() == null ? 0 : c.getDurationSec()).sum() / 60.0);
        int goalXp = user.getDailyGoalXp() == null ? 30 : user.getDailyGoalXp();
        int streak = user.getStreak() == null ? 0 : user.getStreak();

        // 近 7 天 XP
        List<DayXpVO> weekXp = weekXp(all, today);

        // 各语种进度（仅展示已加入语种）
        List<Enrollment> enrollments = enrollmentService.listByUser(userId);
        Map<String, Language> langMap = new HashMap<>();
        for (Language l : languageMapper.selectList(null)) {
            langMap.put(l.getCode(), l);
        }
        List<LessonCompletion> allCompletions = completionMapper.selectList(
                new QueryWrapper<LessonCompletion>().eq("user_id", userId));
        Map<Long, Lesson> lessonCache = new HashMap<>();
        for (Lesson l : lessonMapper.selectList(null)) {
            lessonCache.put(l.getId(), l);
        }
        Map<String, List<LessonCompletion>> compByLang = new HashMap<>();
        for (LessonCompletion c : allCompletions) {
            Lesson l = lessonCache.get(c.getLessonId());
            if (l != null) {
                compByLang.computeIfAbsent(l.getLangCode(), k -> new ArrayList<>()).add(c);
            }
        }

        List<WordMastery> cards = wordMasteryMapper.selectList(
                new QueryWrapper<WordMastery>().eq("user_id", userId));
        List<LangProgressVO> langVOs = new ArrayList<>();
        for (Enrollment e : enrollments) {
            Language lang = langMap.get(e.getLangCode());
            List<Lesson> releasedLessons = lessonMapper.selectList(new QueryWrapper<Lesson>()
                    .eq("lang_code", e.getLangCode()).eq("released", true));
            List<LessonCompletion> done = compByLang.getOrDefault(e.getLangCode(), List.of());
            int percent = releasedLessons.isEmpty() ? 0
                    : (int) Math.round(done.size() * 100.0 / releasedLessons.size());
            Integer avgAccuracy = done.isEmpty() ? null
                    : (int) Math.round(done.stream().mapToInt(LessonCompletion::getAccuracy).average().orElse(0));
            int mastered = (int) cards.stream()
                    .filter(c -> e.getLangCode().equals(c.getLangCode()) && Boolean.TRUE.equals(c.getMastered()))
                    .count();
            int learning = (int) cards.stream()
                    .filter(c -> e.getLangCode().equals(c.getLangCode()) && !Boolean.TRUE.equals(c.getMastered()))
                    .count();
            langVOs.add(new LangProgressVO(e.getLangCode(),
                    lang == null ? e.getLangCode() : lang.getNameZh(),
                    lang == null ? null : lang.getColor(),
                    e.getPlacementLevel(), done.size(), releasedLessons.size(), percent,
                    mastered, learning, avgAccuracy));
        }

        // 四模块：语法/听力/口语取最近 30 条流水均分；词汇取掌握率
        List<ModuleVO> modules = new ArrayList<>();
        for (String type : List.of("GRAMMAR", "LISTENING", "SPEAKING")) {
            int[] scores = all.stream()
                    .filter(a -> type.equals(a.getType()) && a.getScore() != null)
                    .limit(RECENT_MODULE_LIMIT).mapToInt(Activity::getScore).toArray();
            Integer avg = scores.length == 0 ? null : (int) Math.round(Arrays.stream(scores).average().orElse(0));
            modules.add(new ModuleVO(type, moduleLabel(type), avg, scores.length));
        }
        int vocabTotal = cards.size();
        int vocabMastered = (int) cards.stream().filter(c -> Boolean.TRUE.equals(c.getMastered())).count();
        Integer vocabScore = vocabTotal == 0 ? null : (int) Math.round(vocabMastered * 100.0 / vocabTotal);
        modules.add(0, new ModuleVO("VOCAB", "词汇", vocabScore, vocabTotal));

        // 词汇分布
        List<VocabLangVO> vocabByLang = new ArrayList<>();
        int dueTotal = 0;
        for (Enrollment e : enrollments) {
            List<WordMastery> langCards = cards.stream()
                    .filter(c -> e.getLangCode().equals(c.getLangCode())).toList();
            int mastered = (int) langCards.stream().filter(c -> Boolean.TRUE.equals(c.getMastered())).count();
            int due = (int) langCards.stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getMastered())
                            && c.getDueDate() != null && !c.getDueDate().isAfter(today))
                    .count();
            dueTotal += due;
            vocabByLang.add(new VocabLangVO(e.getLangCode(), mastered, langCards.size() - mastered, due));
        }

        LevelVO level = new LevelVO((int) (totalXp / XP_PER_LEVEL) + 1, totalXp,
                (int) (totalXp % XP_PER_LEVEL), XP_PER_LEVEL,
                (int) Math.round(totalXp % XP_PER_LEVEL * 100.0 / XP_PER_LEVEL));
        TodayVO todayVO = new TodayVO(todayXp, goalXp, todayXp >= goalXp,
                Math.max(0, goalXp - todayXp), todayMinutes, todayCompletions.size());

        return new DashboardVO(level, streak, todayVO, weekXp, langVOs, modules,
                new VocabVO(vocabMastered, vocabTotal - vocabMastered, dueTotal, vocabByLang));
    }

    private List<DayXpVO> weekXp(List<Activity> activities, LocalDate today) {
        Map<LocalDate, Integer> sum = new HashMap<>();
        for (Activity a : activities) {
            if (a.getCreatedAt() == null || a.getXp() == null || a.getXp() <= 0) {
                continue;
            }
            LocalDate d = a.getCreatedAt().toLocalDate();
            sum.merge(d, a.getXp(), Integer::sum);
        }
        List<DayXpVO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            result.add(new DayXpVO(d, sum.getOrDefault(d, 0)));
        }
        return result;
    }

    static String moduleLabel(String code) {
        return switch (code) {
            case "VOCAB" -> "词汇";
            case "GRAMMAR" -> "语法";
            case "LISTENING" -> "听力";
            case "SPEAKING" -> "口语";
            default -> code;
        };
    }
}
