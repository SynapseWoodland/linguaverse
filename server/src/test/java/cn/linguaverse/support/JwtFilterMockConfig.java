package cn.linguaverse.support;

import cn.linguaverse.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Web 切片共用：用 Mockito mock 替换真实 JwtAuthFilter，使 @WebMvcTest 能导入真实 SecurityConfig。
 * 默认 mock 不做任何事（匿名）；调用 {@link #authenticate(JwtAuthFilter, long)} 模拟已登录。
 */
@TestConfiguration
public class JwtFilterMockConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return Mockito.mock(JwtAuthFilter.class);
    }

    /** 匿名放行（不设认证）：请求继续走安全链，由授权层对受保护端点返回 401。 */
    public static void continueAnonymously(JwtAuthFilter filter) throws Exception {
        doAnswer(inv -> {
            SecurityContextHolder.clearContext();
            invokeChain(inv);
            return null;
        }).when(filter).doFilter(any(), any(), any());
    }

    /** 让 mock 过滤器把指定 userId 放入 SecurityContext 后放行，模拟合法 JWT。 */
    public static void authenticate(JwtAuthFilter filter, long userId) throws Exception {
        doAnswer(inv -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            invokeChain(inv);
            return null;
        }).when(filter).doFilter(any(), any(), any());
    }

    private static void invokeChain(org.mockito.invocation.InvocationOnMock inv) {
        try {
            HttpServletRequest req = inv.getArgument(0);
            HttpServletResponse res = inv.getArgument(1);
            inv.getArgument(2, FilterChain.class).doFilter(req, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
