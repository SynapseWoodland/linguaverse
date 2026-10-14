package cn.linguaverse.course.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.dto.PlacementViews.PlacementPaperVO;
import cn.linguaverse.course.dto.PlacementViews.ResultVO;
import cn.linguaverse.course.entity.Language;
import cn.linguaverse.course.entity.PlacementQuestion;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.course.mapper.PlacementQuestionMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.PlacementResult;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.PlacementResultMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 定级判分边界：0-3→L1、4-6→L2、7-10→L3（阈值 0.4/0.7），入参与语种校验。 */
@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    private static final long USER_ID = 9L;
    private static final String LANG = "en";
    private static final int TOTAL = 10;

    @Mock
    LanguageMapper languageMapper;
    @Mock
    PlacementQuestionMapper questionMapper;
    @Mock
    PlacementResultMapper resultMapper;
    @Mock
    ActivityMapper activityMapper;
    @Mock
    EnrollmentService enrollmentService;

    PlacementService service;

    @BeforeEach
    void setUp() {
        service = new PlacementService(languageMapper, questionMapper, resultMapper,
                activityMapper, enrollmentService, new ObjectMapper());
    }

    private List<PlacementQuestion> tenQuestions() {
        List<PlacementQuestion> list = new ArrayList<>();
        for (int i = 1; i <= TOTAL; i++) {
            PlacementQuestion q = new PlacementQuestion();
            q.setId((long) i);
            q.setLangCode(LANG);
            q.setSortNo(i);
            q.setStem("Q" + i);
            q.setOptionsJson("[\"a\",\"b\",\"c\",\"d\"]");
            q.setAnswerIndex(0); // 约定：选 0 即答对
            list.add(q);
        }
        return list;
    }

    private List<Integer> answersWithCorrect(int correctCount) {
        List<Integer> answers = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            answers.add(i < correctCount ? 0 : 1);
        }
        return answers;
    }

    private void stubPaper() {
        when(languageMapper.selectById(LANG)).thenReturn(mock(Language.class));
        when(questionMapper.selectList(any(QueryWrapper.class))).thenReturn(tenQuestions());
    }

    private ResultVO submit(int correctCount) {
        stubPaper();
        return service.submit(USER_ID, LANG, answersWithCorrect(correctCount));
    }

    @Test
    @DisplayName("0 题正确 → L1（保底）")
    void zeroCorrectSuggestL1() {
        ResultVO vo = submit(0);
        assertThat(vo.correctCount()).isZero();
        assertThat(vo.suggestedLevel()).isEqualTo(1);
        assertThat(vo.enrolled()).isTrue();
        verify(enrollmentService).upsert(USER_ID, LANG, 1);
    }

    @Test
    @DisplayName("3 题正确（0.3 < 0.4）→ L1")
    void threeCorrectSuggestL1() {
        assertThat(submit(3).suggestedLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("恰好 4 题正确（阈值 0.4）→ L2，落库正确数/总数/级别一致")
    void fourCorrectSuggestL2() {
        ResultVO vo = submit(4);
        assertThat(vo.suggestedLevel()).isEqualTo(2);
        assertThat(vo.total()).isEqualTo(TOTAL);
        verify(enrollmentService).upsert(USER_ID, LANG, 2);

        ArgumentCaptor<PlacementResult> captor = ArgumentCaptor.forClass(PlacementResult.class);
        verify(resultMapper).insert(captor.capture());
        PlacementResult saved = captor.getValue();
        assertThat(saved.getCorrectCount()).isEqualTo(4);
        assertThat(saved.getTotal()).isEqualTo(TOTAL);
        assertThat(saved.getSuggestedLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("6 题正确（0.6 < 0.7）→ L2")
    void sixCorrectSuggestL2() {
        assertThat(submit(6).suggestedLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("恰好 7 题正确（阈值 0.7）→ L3")
    void sevenCorrectSuggestL3() {
        assertThat(submit(7).suggestedLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("全对 → L3")
    void fullCorrectSuggestL3() {
        assertThat(submit(10).suggestedLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("答案数量与题数不符 → 400")
    void answerCountMismatch400() {
        stubPaper();
        List<Integer> nine = answersWithCorrect(0).subList(0, 9);
        assertThatThrownBy(() -> service.submit(USER_ID, LANG, nine))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("选项下标越界（4）→ 400")
    void choiceOutOfRange400() {
        stubPaper();
        List<Integer> answers = answersWithCorrect(0);
        answers.set(2, 4);
        assertThatThrownBy(() -> service.submit(USER_ID, LANG, answers))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("答案缺项（null）→ 400")
    void nullAnswer400() {
        stubPaper();
        List<Integer> answers = answersWithCorrect(0);
        answers.set(0, null);
        assertThatThrownBy(() -> service.submit(USER_ID, LANG, answers))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("语种不存在 → 404")
    void unknownLang404() {
        when(languageMapper.selectById("xx")).thenReturn(null);
        assertThatThrownBy(() -> service.submit(USER_ID, "xx", List.of(0)))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("语种无定级题 → 404")
    void noQuestions404() {
        when(languageMapper.selectById(LANG)).thenReturn(mock(Language.class));
        when(questionMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());
        assertThatThrownBy(() -> service.submit(USER_ID, LANG, List.of(0)))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("试卷下发不含 answerIndex（NFR-12）")
    void paperDoesNotLeakAnswers() throws Exception {
        stubPaper();
        PlacementPaperVO paper = service.paper(LANG);
        assertThat(paper.total()).isEqualTo(TOTAL);
        assertThat(paper.questions()).hasSize(TOTAL);
        assertThat(paper.questions().get(0).options()).containsExactly("a", "b", "c", "d");

        String json = new ObjectMapper().writeValueAsString(paper);
        assertThat(json).contains("\"stem\"").contains("\"options\"");
        assertThat(json).doesNotContain("answerIndex").doesNotContain("answer");
    }
}
