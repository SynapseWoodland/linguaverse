package cn.linguaverse.course;

import cn.linguaverse.common.BizException;
import cn.linguaverse.config.SecurityConfig;
import cn.linguaverse.course.dto.QuizViews.GrammarItemResult;
import cn.linguaverse.course.dto.QuizViews.GrammarResultVO;
import cn.linguaverse.course.service.QuizService;
import cn.linguaverse.security.JwtAuthFilter;
import cn.linguaverse.support.JwtFilterMockConfig;
import cn.linguaverse.support.SliceMappersMockConfig;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web 切片错误码矩阵（真实 SecurityConfig + GlobalExceptionHandler，Service 全 mock，不连库）：
 * 401 无/坏 token、400 畸形 JSON/空答案、403/404 业务码、200 成功响应。
 */
@WebMvcTest(QuizController.class)
@Import({SecurityConfig.class, JwtFilterMockConfig.class, SliceMappersMockConfig.class})
@TestPropertySource(properties = {
        "linguaverse.cors.allowed-origins=http://localhost:5173",
        "spring.main.allow-bean-definition-overriding=true"
})
class QuizApiSliceTest {

    private static final long USER_ID = 9L;
    private static final String URL = "/lessons/1/grammar/submit";

    @Autowired MockMvc mvc;
    @Autowired JwtAuthFilter jwtFilter;
    @MockBean QuizService quizService;

    @BeforeEach
    void resetFilter() {
        Mockito.reset(jwtFilter);
    }

    @Test
    @DisplayName("无 token 访问受保护端点 → 401 统一响应体")
    void noToken401() throws Exception {
        JwtFilterMockConfig.continueAnonymously(jwtFilter);

        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":101,\"choiceIndex\":0}]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    @DisplayName("畸形 JSON → 400 而不是 500（授权先于参数解析，需携带合法 token）")
    void malformedJson400() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);

        mvc.perform(post(URL).contentType("application/json").content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求体格式错误，请发送合法 JSON"));
    }

    @Test
    @DisplayName("空答案列表 → 400 校验信息")
    void emptyAnswers400() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);

        mvc.perform(post(URL).contentType("application/json").content("{\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请至少回答一道题"));
    }

    @Test
    @DisplayName("选项下标为负 → 400")
    void negativeChoice400() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);

        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":101,\"choiceIndex\":-1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("已认证 + 业务 403（锁定课时）→ 403 统一响应")
    void serviceForbidden403() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);
        when(quizService.gradeGrammar(eq(USER_ID), eq(1L), anyList()))
                .thenThrow(BizException.forbidden("课时未解锁，请先完成上一节课"));

        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":101,\"choiceIndex\":0}]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("课时未解锁，请先完成上一节课"));
    }

    @Test
    @DisplayName("已认证 + 业务 404（课时不存在）→ 404")
    void serviceNotFound404() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);
        when(quizService.gradeGrammar(eq(USER_ID), eq(1L), anyList()))
                .thenThrow(BizException.notFound("课时不存在"));

        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":101,\"choiceIndex\":0}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("已认证 + 业务 400（混入他课题目）→ 400")
    void serviceBadRequest400() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);
        when(quizService.gradeGrammar(eq(USER_ID), eq(1L), anyList()))
                .thenThrow(BizException.badRequest("存在不属于本课时的题目: 999"));

        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":999,\"choiceIndex\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("存在不属于本课时的题目: 999"));
    }

    @Test
    @DisplayName("成功提交 → 200 信封 code=0，回执含正确答案与解析")
    void successEnvelope() throws Exception {
        JwtFilterMockConfig.authenticate(jwtFilter, USER_ID);
        GrammarResultVO vo = new GrammarResultVO(1L, 1, 1, 100,
                List.of(new GrammarItemResult(101L, 0, true, 0, "be 动词用 am")));
        when(quizService.gradeGrammar(eq(USER_ID), eq(1L), anyList())).thenReturn(vo);

        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":101,\"choiceIndex\":0}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score").value(100))
                .andExpect(jsonPath("$.data.results[0].correctIndex").value(0))
                .andExpect(jsonPath("$.data.results[0].explanation").value("be 动词用 am"));
    }
}
