package cn.linguaverse.learn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EnrollRequest {

    @NotBlank(message = "请选择语种")
    @Pattern(regexp = "^(en|ja|ko)$", message = "语种仅支持 en/ja/ko")
    private String langCode;

    public String getLangCode() { return langCode; }
    public void setLangCode(String langCode) { this.langCode = langCode; }
}
