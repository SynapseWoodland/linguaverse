package cn.linguaverse.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("lesson_completions")
public class LessonCompletion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long lessonId;
    private Integer xp;
    private Integer accuracy;
    private Integer wordsCount;
    private Integer durationSec;
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Integer getXp() { return xp; }
    public void setXp(Integer xp) { this.xp = xp; }
    public Integer getAccuracy() { return accuracy; }
    public void setAccuracy(Integer accuracy) { this.accuracy = accuracy; }
    public Integer getWordsCount() { return wordsCount; }
    public void setWordsCount(Integer wordsCount) { this.wordsCount = wordsCount; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
