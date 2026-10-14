package cn.linguaverse.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("enrollments")
public class Enrollment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String langCode;
    private Integer placementLevel;
    private LocalDateTime startedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLangCode() { return langCode; }
    public void setLangCode(String langCode) { this.langCode = langCode; }
    public Integer getPlacementLevel() { return placementLevel; }
    public void setPlacementLevel(Integer placementLevel) { this.placementLevel = placementLevel; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
