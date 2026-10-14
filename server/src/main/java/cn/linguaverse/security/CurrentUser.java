package cn.linguaverse.security;

import cn.linguaverse.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 从安全上下文取当前登录用户 ID。 */
public final class CurrentUser {

    private CurrentUser() {}

    public static long requireId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw BizException.unauthorized("请先登录");
        }
        return userId;
    }
}
