package cn.linguaverse.learn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class CompletionViews {
    private CompletionViews() {}

    public record CompleteRequest(
            @NotNull(message = "缺少学习时长")
            @Min(value = 0, message = "学习时长不能为负")
            @Max(value = 14400, message = "单课时学习时长异常")
            Integer durationSec) {}

    public record XpDetail(int base, int bonus, int total) {}

    public record NextLessonVO(long id, int seqNo, String slug, String title) {}

    public record CompleteVO(long lessonId, boolean alreadyCompleted, XpDetail xp, int accuracy,
                             int wordsCount, int durationSec, int streak,
                             LocalDateTime completedAt, NextLessonVO nextLesson,
                             List<String> newAchievements) {}
}
