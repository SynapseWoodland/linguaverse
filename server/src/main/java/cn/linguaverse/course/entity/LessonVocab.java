package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("lesson_vocab")
public class LessonVocab {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long lessonId;
    private String word;
    private String phonetic;
    private String meaningZh;
    private String example;
    private String exampleZh;
    private Integer sortNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }
    public String getMeaningZh() { return meaningZh; }
    public void setMeaningZh(String meaningZh) { this.meaningZh = meaningZh; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
    public String getExampleZh() { return exampleZh; }
    public void setExampleZh(String exampleZh) { this.exampleZh = exampleZh; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
