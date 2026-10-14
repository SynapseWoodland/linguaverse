package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("listening_materials")
public class ListeningMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long lessonId;
    private String title;
    /** 逐句 JSON：[{speaker, text, zh}] */
    private String linesJson;
    private String questionStem;
    private String optionsJson;
    private Integer answerIndex;
    private String explanation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLinesJson() { return linesJson; }
    public void setLinesJson(String linesJson) { this.linesJson = linesJson; }
    public String getQuestionStem() { return questionStem; }
    public void setQuestionStem(String questionStem) { this.questionStem = questionStem; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public Integer getAnswerIndex() { return answerIndex; }
    public void setAnswerIndex(Integer answerIndex) { this.answerIndex = answerIndex; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
