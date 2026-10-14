package cn.linguaverse.user;

import cn.linguaverse.common.BizException;
import cn.linguaverse.config.SecurityConfig;
import cn.linguaverse.security.JwtAuthFilter;
import cn.linguaverse.support.JwtFilterMockConfig;
import cn.linguaverse.support.SliceMappersMockConfig;
import cn.linguaverse.user.dto.UserViews.AuthResponse;
import cn.linguaverse.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 认证端点 Web 切片：注册校验、畸形 JSON、409 重复用户名、登录 429 限流信封。 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilterMockConfig.class, SliceMappersMockConfig.class})
@TestPropertySource(properties = {
        "linguaverse.cors.allowed-origins=http://localhost:5173",
        "spring.main.allow-bean-definition-overriding=true"
})
class AuthApiSliceTest {

    @Autowired MockMvc mvc;
    @Autowired JwtAuthFilter jwtFilter;
    @MockBean AuthService authService;

    @BeforeEach
    void resetFilter() throws Exception {
        // 公开端点虽放行，mock 过滤器仍需让请求继续走链
        Mockito.reset(jwtFilter);
        JwtFilterMockConfig.continueAnonymously(jwtFilter);
    }

    @Test
    @DisplayName("注册：空用户名 → 400 校验信息（同时命中 NotBlank/Size，文案必含「用户名」）")
    void registerBlankUsername400() throws Exception {
        mvc.perform(post("/auth/register").contentType("application/json")
                        .content("{\"username\":\"\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户名")));
    }

    @Test
    @DisplayName("注册：用户名含非法字符 → 400")
    void registerIllegalUsername400() throws Exception {
        mvc.perform(post("/auth/register").contentType("application/json")
                        .content("{\"username\":\"bad name!\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("注册：密码过短 → 400")
    void registerShortPassword400() throws Exception {
        mvc.perform(post("/auth/register").contentType("application/json")
                        .content("{\"username\":\"gooduser\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("注册：畸形 JSON → 400")
    void registerMalformedJson400() throws Exception {
        mvc.perform(post("/auth/register").contentType("application/json").content("{x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请求体格式错误，请发送合法 JSON"));
    }

    @Test
    @DisplayName("注册：用户名重复 → 409 信封")
    void registerDuplicate409() throws Exception {
        when(authService.register(any())).thenThrow(BizException.conflict("用户名已被注册"));

        mvc.perform(post("/auth/register").contentType("application/json")
                        .content("{\"username\":\"dupe\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("用户名已被注册"));
    }

    @Test
    @DisplayName("注册成功（公开端点免 token）→ 200 code=0")
    void registerSuccess() throws Exception {
        when(authService.register(any()))
                .thenReturn(new AuthResponse("jwt-token", null));

        mvc.perform(post("/auth/register").contentType("application/json")
                        .content("{\"username\":\"newbie\",\"password\":\"secret123\",\"nickname\":\"小新\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("登录：触发限流 → 429 信封")
    void loginRateLimited429() throws Exception {
        when(authService.login(eq("demo"), eq("wrong"), org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(BizException.tooManyRequests("登录尝试过于频繁，请 10 分钟后再试"));

        mvc.perform(post("/auth/login").contentType("application/json")
                        .content("{\"username\":\"demo\",\"password\":\"wrong\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("登录尝试过于频繁，请 10 分钟后再试"));
    }
}
