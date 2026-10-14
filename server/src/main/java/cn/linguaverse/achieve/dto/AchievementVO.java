package cn.linguaverse.achieve.dto;

import java.time.LocalDateTime;

/** Task 10：成就墙条目。current/target 驱动前端进度条。 */
public record AchievementVO(
        String code,
        String nameZh,
        String descZh,
        String icon,
        boolean unlocked,
        LocalDateTime unlockedAt,
        long current,
        long target) {
}
