package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("speaking_sentences")
public class SpeakingSentence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long lessonId;
    private String text;
    private String textZh;
    private Integer sortNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTextZh() { return textZh; }
    public void setTextZh(String textZh) { this.textZh = textZh; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
