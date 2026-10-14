package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("levels")
public class Level {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String langCode;
    private Integer levelNo;
    private String title;
    private Boolean released;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLangCode() { return langCode; }
    public void setLangCode(String langCode) { this.langCode = langCode; }
    public Integer getLevelNo() { return levelNo; }
    public void setLevelNo(Integer levelNo) { this.levelNo = levelNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Boolean getReleased() { return released; }
    public void setReleased(Boolean released) { this.released = released; }
}
