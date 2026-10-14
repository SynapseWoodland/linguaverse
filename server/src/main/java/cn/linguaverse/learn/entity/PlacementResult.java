package cn.linguaverse.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("placement_results")
public class PlacementResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String langCode;
    private Integer correctCount;
    private Integer total;
    private Integer suggestedLevel;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLangCode() { return langCode; }
    public void setLangCode(String langCode) { this.langCode = langCode; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getSuggestedLevel() { return suggestedLevel; }
    public void setSuggestedLevel(Integer suggestedLevel) { this.suggestedLevel = suggestedLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
