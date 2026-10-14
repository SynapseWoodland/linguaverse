package cn.linguaverse.stats.dto;

import java.time.LocalDate;
import java.util.List;

/** Task 8：仪表盘聚合与今日推荐视图。 */
public final class StatsViews {
    private StatsViews() {}

    // ---------- 仪表盘 ----------

    public record LevelVO(int level, long totalXp, int xpIntoLevel, int xpForNext, int progressPercent) {}

    public record TodayVO(int xp, int goalXp, boolean goalReached, int remainingXp,
                          int minutes, int completedLessons) {}

    public record DayXpVO(LocalDate date, int xp) {}

    public record LangProgressVO(String langCode, String nameZh, String color, Integer placementLevel,
                                 int completedLessons, int totalLessons, int percent,
                                 int masteredWords, int learningWords, Integer avgAccuracy) {}

    public record ModuleVO(String code, String labelZh, Integer avgScore, int attempts) {}

    public record VocabLangVO(String langCode, int mastered, int learning, int dueToday) {}

    public record VocabVO(int mastered, int learning, int dueToday, List<VocabLangVO> byLang) {}

    public record DashboardVO(LevelVO level, int streak, TodayVO today, List<DayXpVO> weekXp,
                              List<LangProgressVO> languages, List<ModuleVO> modules, VocabVO vocab) {}

    // ---------- 今日推荐 ----------

    public record RecommendationVO(String key, String type, String title, String reason,
                                   String ctaText, String deeplink, int priority) {}

    public record TodayPlanVO(LocalDate date, int todayXp, int goalXp, boolean goalReached,
                              String message, List<RecommendationVO> recommendations) {}
}
