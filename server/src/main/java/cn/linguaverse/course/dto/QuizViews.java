package cn.linguaverse.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/** Task 6：语法/听力/口语判题请求与响应。提交后的响应才允许包含正确答案与解析。 */
public final class QuizViews {
    private QuizViews() {}

    // ---------- 语法 ----------

    public record GrammarAnswerItem(
            @NotNull(message = "缺少题目 ID") Long questionId,
            @NotNull(message = "缺少作答") @Min(value = 0, message = "选项下标非法") Integer choiceIndex) {}

    public record GrammarSubmitRequest(
            @NotEmpty(message = "请至少回答一道题") @Valid List<GrammarAnswerItem> answers) {}

    public record GrammarItemResult(long questionId, int choiceIndex, boolean correct,
                                    int correctIndex, String explanation) {}

    public record GrammarResultVO(long lessonId, int correctCount, int total, int score,
                                  List<GrammarItemResult> results) {}

    // ---------- 听力 ----------

    public record ListeningSubmitRequest(
            @NotNull(message = "缺少作答") @Min(value = 0, message = "选项下标非法") Integer choiceIndex) {}

    public record ListeningResultVO(long lessonId, boolean correct, int choiceIndex,
                                    int correctIndex, int score, String explanation) {}

    // ---------- 口语 ----------

    public record SpeakingSentenceItem(
            @NotNull(message = "缺少句子 ID") Long sentenceId,
            String transcript,
            @Min(value = 0, message = "分数必须在 0-100") @Max(value = 100, message = "分数必须在 0-100")
            Integer score,
            @Pattern(regexp = "^(NEED_PRACTICE|OK|FLUENT)$", message = "自评档位非法")
            String rating) {}

    public record SpeakingSubmitRequest(
            @NotNull @Pattern(regexp = "^(RECOGNITION|SELF)$", message = "评分模式仅支持 RECOGNITION/SELF")
            String mode,
            @NotEmpty(message = "请至少跟读一句") @Valid List<SpeakingSentenceItem> sentences) {}

    public record SpeakingSentenceResult(long sentenceId, Integer score, String feedback) {}

    public record SpeakingResultVO(long lessonId, String mode, int score,
                                   List<SpeakingSentenceResult> results) {}
}
