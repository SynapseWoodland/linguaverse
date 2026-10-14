package cn.linguaverse.course.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 定级题与判分视图（选项下发、答案永不下发）。 */
public final class PlacementViews {
    private PlacementViews() {}

    public record PlacementQuestionVO(long id, int sortNo, String stem, List<String> options) {}

    public record PlacementPaperVO(String langCode, int total, List<PlacementQuestionVO> questions) {}

    public record SubmitRequest(@NotNull(message = "答案不能为空") List<Integer> answers) {}

    public record ResultVO(String langCode, int correctCount, int total, int suggestedLevel,
                           boolean enrolled) {}
}
