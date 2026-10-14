package cn.linguaverse.security;

import cn.linguaverse.common.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 登录限流：按 用户名+IP 计数，达到阈值锁 429，登录成功清零，锁窗过期放行。 */
class LoginRateLimiterTest {

    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiter();
        ReflectionTestUtils.setField(limiter, "maxFailures", 5);
        ReflectionTestUtils.setField(limiter, "lockMinutes", 10);
    }

    private void failTimes(String username, String ip, int times) {
        for (int i = 0; i < times; i++) {
            limiter.recordFailure(username, ip);
        }
    }

    @Test
    @DisplayName("失败 4 次仍放行，第 5 次达到阈值 → 429")
    void fifthFailureLocks() {
        failTimes("demo", "1.1.1.1", 4);
        assertThatCode(() -> limiter.checkAllowed("demo", "1.1.1.1")).doesNotThrowAnyException();

        limiter.recordFailure("demo", "1.1.1.1");
        assertThatThrownBy(() -> limiter.checkAllowed("demo", "1.1.1.1"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(429));
    }

    @Test
    @DisplayName("登录成功清零计数 → 重新放行")
    void successClearsAttempts() {
        failTimes("demo", "1.1.1.1", 5);
        limiter.recordSuccess("demo", "1.1.1.1");
        assertThatCode(() -> limiter.checkAllowed("demo", "1.1.1.1")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("用户名大小写/首尾空格归一化，同一键计数")
    void usernameNormalized() {
        failTimes(" Demo ", "1.1.1.1", 5);
        assertThatThrownBy(() -> limiter.checkAllowed("demo", "1.1.1.1"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(429));
    }

    @Test
    @DisplayName("不同 IP 互不影响")
    void differentIpIsolated() {
        failTimes("demo", "1.1.1.1", 5);
        assertThatCode(() -> limiter.checkAllowed("demo", "2.2.2.2")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("锁定窗口过后自动放行")
    void lockWindowExpiryReleases() {
        // lockMinutes=-1 使窗口必然过期（避免 0 在毫秒级时钟分辨率下的时间竞争）
        ReflectionTestUtils.setField(limiter, "lockMinutes", -1);
        failTimes("demo", "1.1.1.1", 5);
        assertThatCode(() -> limiter.checkAllowed("demo", "1.1.1.1")).doesNotThrowAnyException();
    }
}
