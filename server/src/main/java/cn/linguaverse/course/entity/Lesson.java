package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("lessons")
public class Lesson {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long unitId;
    private Integer lessonNo;
    private String langCode;
    private Integer seqNo;
    private String slug;
    private String title;
    private String titleNative;
    private String scene;
    private Boolean released;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public Integer getLessonNo() { return lessonNo; }
    public void setLessonNo(Integer lessonNo) { this.lessonNo = lessonNo; }
    public String getLangCode() { return langCode; }
    public void setLangCode(String langCode) { this.langCode = langCode; }
    public Integer getSeqNo() { return seqNo; }
    public void setSeqNo(Integer seqNo) { this.seqNo = seqNo; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTitleNative() { return titleNative; }
    public void setTitleNative(String titleNative) { this.titleNative = titleNative; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Boolean getReleased() { return released; }
    public void setReleased(Boolean released) { this.released = released; }
}
