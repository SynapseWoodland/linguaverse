package cn.linguaverse.user.dto;

import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 用户相关响应视图（不含密码哈希等敏感字段）。 */
public final class UserViews {
    private UserViews() {}

    public record UserVO(Long id, String username, String nickname, String bio, String avatarSeed,
                         String role, Integer dailyGoalXp, Integer streak,
                         Boolean onboarded,
                         LocalDate lastStudyDate, LocalDateTime createdAt) {
        public static UserVO of(User u) {
            return new UserVO(u.getId(), u.getUsername(), u.getNickname(), u.getBio(),
                    u.getAvatarSeed(), u.getRole(), u.getDailyGoalXp(), u.getStreak(),
                    Boolean.TRUE.equals(u.getOnboarded()),
                    u.getLastStudyDate(), u.getCreatedAt());
        }
    }

    public record AuthResponse(String token, UserVO user) {}

    public record EnrollmentVO(String langCode, Integer placementLevel, LocalDateTime startedAt) {
        public static EnrollmentVO of(Enrollment e) {
            return new EnrollmentVO(e.getLangCode(), e.getPlacementLevel(), e.getStartedAt());
        }
    }

    public record MeResponse(UserVO user, List<EnrollmentVO> enrollments) {}
}
