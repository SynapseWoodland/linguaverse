package cn.linguaverse.security;

import cn.linguaverse.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录限流（MVP 单节点内存实现）：
 * 按 用户名+IP 记录失败次数，达到 maxFailures 后锁定 lockMinutes 分钟 → 429。
 */
@Component
public class LoginRateLimiter {

    private record Attempt(int failures, long firstFailAt) {}

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Value("${linguaverse.login.max-failures}")
    private int maxFailures;

    @Value("${linguaverse.login.lock-minutes}")
    private int lockMinutes;

    public void checkAllowed(String username, String ip) {
        Attempt a = attempts.get(key(username, ip));
        if (a == null) return;
        long elapsed = System.currentTimeMillis() - a.firstFailAt();
        if (elapsed > lockMinutes * 60_000L) {
            attempts.remove(key(username, ip));
            return;
        }
        if (a.failures() >= maxFailures) {
            throw BizException.tooManyRequests("登录失败次数过多，请 " + lockMinutes + " 分钟后再试");
        }
    }

    public void recordFailure(String username, String ip) {
        String k = key(username, ip);
        attempts.merge(k, new Attempt(1, System.currentTimeMillis()),
                (old, fresh) -> new Attempt(old.failures() + 1, old.firstFailAt()));
    }

    public void recordSuccess(String username, String ip) {
        attempts.remove(key(username, ip));
    }

    private String key(String username, String ip) {
        return username.trim().toLowerCase() + "|" + ip;
    }
}
