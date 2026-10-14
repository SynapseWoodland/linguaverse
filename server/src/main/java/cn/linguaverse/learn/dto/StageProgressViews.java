package cn.linguaverse.learn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public final class StageProgressViews {
    private StageProgressViews() {}

    public static final List<String> STAGES = List.of("VOCAB", "GRAMMAR", "LISTENING", "SPEAKING");

    public record ProgressRequest(
            @NotBlank(message = "缺少环节")
            @Pattern(regexp = "^(VOCAB|GRAMMAR|LISTENING|SPEAKING)$", message = "非法环节")
            String stage,
            @NotNull(message = "缺少正确率")
            @Min(value = 0, message = "正确率不能小于 0")
            @Max(value = 100, message = "正确率不能大于 100")
            Integer score) {}

    public record StageVO(String stage, Integer score) {}

    public record ProgressResponse(long lessonId, List<StageVO> stages) {}
}
