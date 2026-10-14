package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("units")
public class Unit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long levelId;
    private Integer unitNo;
    private String title;
    private String titleNative;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public Integer getUnitNo() { return unitNo; }
    public void setUnitNo(Integer unitNo) { this.unitNo = unitNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTitleNative() { return titleNative; }
    public void setTitleNative(String titleNative) { this.titleNative = titleNative; }
}
