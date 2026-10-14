package cn.linguaverse.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateMeRequest {

    @Size(min = 1, max = 32, message = "昵称长度需为 1-32 个字符")
    private String nickname;

    @Size(max = 200, message = "简介最长 200 个字符")
    private String bio;

    @Size(min = 1, max = 2, message = "头像种子为 1-2 个字符")
    private String avatarSeed;

    @Min(value = 5, message = "每日目标至少 5 XP")
    @Max(value = 200, message = "每日目标最多 200 XP")
    private Integer dailyGoalXp;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarSeed() { return avatarSeed; }
    public void setAvatarSeed(String avatarSeed) { this.avatarSeed = avatarSeed; }
    public Integer getDailyGoalXp() { return dailyGoalXp; }
    public void setDailyGoalXp(Integer dailyGoalXp) { this.dailyGoalXp = dailyGoalXp; }
}
