package cn.linguaverse.course.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("languages")
public class Language {
    @TableId
    private String code;
    private String nameZh;
    private String nameNative;
    private String color;
    private Integer sortNo;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }
    public String getNameNative() { return nameNative; }
    public void setNameNative(String nameNative) { this.nameNative = nameNative; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
