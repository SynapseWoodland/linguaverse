package cn.linguaverse.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Onboarding 三路径：
 * PLACEMENT —— 先做定级测试（建 enrollment，placementLevel 暂空，由定级提交回填）
 * DIRECT    —— 零基础直学 L1（建 enrollment，placementLevel=1）
 * SKIP      —— 稍后决定，只逛不学（不写 enrollment）
 */
public class OnboardingRequest {

    @NotBlank(message = "请选择语种")
    @Pattern(regexp = "^(en|ja|ko)$", message = "语种仅支持 en/ja/ko")
    private String langCode;

    @NotBlank(message = "请选择开始方式")
    @Pattern(regexp = "^(PLACEMENT|DIRECT|SKIP)$", message = "开始方式仅支持 PLACEMENT/DIRECT/SKIP")
    private String mode;

    public String getLangCode() { return langCode; }
    public void setLangCode(String langCode) { this.langCode = langCode; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
