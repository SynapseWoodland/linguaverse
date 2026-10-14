package cn.linguaverse.learn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public final class ReviewViews {
    private ReviewViews() {}

    public record RateRequest(
            @NotBlank(message = "请选择语种")
            @Pattern(regexp = "^(en|ja|ko)$", message = "语种仅支持 en/ja/ko")
            String langCode,
            @NotBlank(message = "缺少单词")
            String word,
            @NotNull(message = "缺少评分")
            @Pattern(regexp = "^(UNKNOWN|VAGUE|KNOWN)$", message = "评分仅支持 UNKNOWN/VAGUE/KNOWN")
            String rating) {}

    public record WordCardVO(long id, String langCode, String word, String phonetic, String meaningZh,
                             int stage, LocalDate dueDate, String lastRating, boolean mastered) {}

    public record DueQueueVO(LocalDate today, int total, List<WordCardVO> cards) {}
}
