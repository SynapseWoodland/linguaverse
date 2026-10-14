package cn.linguaverse.learn.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.LessonVocab;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.learn.dto.ReviewViews.DueQueueVO;
import cn.linguaverse.learn.dto.ReviewViews.WordCardVO;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** SRS-lite 规则：UNKNOWN→stage0 当天；VAGUE→+1 次日；KNOWN→+2 三天后；stage≥6 mastered。 */
@ExtendWith(MockitoExtension.class)
class SrsServiceTest {

    private static final long USER_ID = 9L;
    private static final String LANG = "en";
    private static final String WORD = "apple";

    @Mock
    WordMasteryMapper wordMasteryMapper;
    @Mock
    LessonVocabMapper lessonVocabMapper;
    @Mock
    LessonMapper lessonMapper;
    @Mock
    EnrollmentService enrollmentService;
    @Mock
    ProgressService progressService;
    @Mock
    AchievementService achievementService;

    SrsService service;
    Lesson lesson;

    @BeforeEach
    void setUp() {
        service = new SrsService(wordMasteryMapper, lessonVocabMapper, lessonMapper,
                enrollmentService, progressService, achievementService);
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setLangCode(LANG);
        lesson.setSeqNo(1);
        lesson.setReleased(true);
    }

    /** 选学 + 词表命中 + 课时已解锁的公共前置。 */
    private void stubAccessibleWord() {
        when(enrollmentService.get(USER_ID, LANG)).thenReturn(mock(Enrollment.class));
        LessonVocab source = new LessonVocab();
        source.setLessonId(lesson.getId());
        source.setWord(WORD);
        source.setPhonetic("/əˈpəl/");
        source.setMeaningZh("苹果");
        when(lessonVocabMapper.findInLang(LANG, WORD)).thenReturn(source);
        when(lessonMapper.selectById(lesson.getId())).thenReturn(lesson);
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(true);
    }

    private WordMastery existingMastery(int stage, boolean mastered) {
        WordMastery m = new WordMastery();
        m.setId(100L);
        m.setUserId(USER_ID);
        m.setLangCode(LANG);
        m.setWord(WORD);
        m.setStage(stage);
        m.setDueDate(LocalDate.now());
        m.setMastered(mastered);
        return m;
    }

    private WordMastery captureUpdate() {
        ArgumentCaptor<WordMastery> captor = ArgumentCaptor.forClass(WordMastery.class);
        verify(wordMasteryMapper).updateById(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("新词 UNKNOWN → insert，stage=0、当天再到期、未掌握")
    void newWordUnknown() {
        stubAccessibleWord();
        when(wordMasteryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        // 模拟 MyBatis 自增主键回填
        when(wordMasteryMapper.insert(any(WordMastery.class))).thenAnswer(inv -> {
            inv.getArgument(0, WordMastery.class).setId(1L);
            return 1;
        });

        WordCardVO card = service.rate(USER_ID, LANG, WORD, "UNKNOWN");

        ArgumentCaptor<WordMastery> captor = ArgumentCaptor.forClass(WordMastery.class);
        verify(wordMasteryMapper).insert(captor.capture());
        WordMastery saved = captor.getValue();
        assertThat(saved.getStage()).isZero();
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getMastered()).isFalse();
        assertThat(saved.getPhonetic()).isEqualTo("/əˈpəl/");
        assertThat(saved.getMeaningZh()).isEqualTo("苹果");
        assertThat(card.lastRating()).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("已有词 UNKNOWN → stage 归零、当天到期、mastered 复位")
    void existingWordUnknownResets() {
        stubAccessibleWord();
        when(wordMasteryMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(existingMastery(3, true));

        service.rate(USER_ID, LANG, WORD, "UNKNOWN");

        WordMastery updated = captureUpdate();
        assertThat(updated.getStage()).isZero();
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.now());
        assertThat(updated.getMastered()).isFalse();
    }

    @Test
    @DisplayName("VAGUE → stage+1、次日到期")
    void vagueAdvancesOne() {
        stubAccessibleWord();
        when(wordMasteryMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(existingMastery(2, false));

        service.rate(USER_ID, LANG, WORD, "VAGUE");

        WordMastery updated = captureUpdate();
        assertThat(updated.getStage()).isEqualTo(3);
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(updated.getMastered()).isFalse();
    }

    @Test
    @DisplayName("KNOWN stage2 → stage4、三天后到期、未掌握")
    void knownAdvancesTwo() {
        stubAccessibleWord();
        when(wordMasteryMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(existingMastery(2, false));

        service.rate(USER_ID, LANG, WORD, "KNOWN");

        WordMastery updated = captureUpdate();
        assertThat(updated.getStage()).isEqualTo(4);
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(updated.getMastered()).isFalse();
    }

    @Test
    @DisplayName("KNOWN stage4 → stage6 达到阈值、标记 mastered")
    void knownToStageSixMasters() {
        stubAccessibleWord();
        when(wordMasteryMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(existingMastery(4, false));

        service.rate(USER_ID, LANG, WORD, "KNOWN");

        WordMastery updated = captureUpdate();
        assertThat(updated.getStage()).isEqualTo(6);
        assertThat(updated.getMastered()).isTrue();
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.now().plusDays(3));
    }

    @Test
    @DisplayName("VAGUE 到 stage6 不触发 mastered（仅 KNOWN 检查阈值）")
    void vagueToStageSixNotMastered() {
        stubAccessibleWord();
        when(wordMasteryMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(existingMastery(5, false));

        service.rate(USER_ID, LANG, WORD, "VAGUE");

        WordMastery updated = captureUpdate();
        assertThat(updated.getStage()).isEqualTo(6);
        assertThat(updated.getMastered()).isFalse();
    }

    @Test
    @DisplayName("未选学 → 403")
    void notEnrolled403() {
        when(enrollmentService.get(USER_ID, LANG)).thenReturn(null);
        assertThatThrownBy(() -> service.rate(USER_ID, LANG, WORD, "KNOWN"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("词不在该语种词表 → 404")
    void unknownWord404() {
        when(enrollmentService.get(USER_ID, LANG)).thenReturn(mock(Enrollment.class));
        when(lessonVocabMapper.findInLang(LANG, "ghost")).thenReturn(null);
        assertThatThrownBy(() -> service.rate(USER_ID, LANG, "ghost", "KNOWN"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("词在未解锁课时 → 403")
    void lockedLessonWord403() {
        when(enrollmentService.get(USER_ID, LANG)).thenReturn(mock(Enrollment.class));
        LessonVocab source = new LessonVocab();
        source.setLessonId(lesson.getId());
        source.setWord(WORD);
        when(lessonVocabMapper.findInLang(LANG, WORD)).thenReturn(source);
        when(lessonMapper.selectById(lesson.getId())).thenReturn(lesson);
        when(progressService.isUnlocked(USER_ID, lesson)).thenReturn(false);

        assertThatThrownBy(() -> service.rate(USER_ID, LANG, WORD, "KNOWN"))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("到期队列：字段映射正确、mastered=null 按 false 处理")
    void dueMapsCards() {
        when(enrollmentService.get(USER_ID, LANG)).thenReturn(mock(Enrollment.class));
        WordMastery a = existingMastery(1, false);
        a.setWord("cat");
        WordMastery b = existingMastery(2, false);
        b.setWord("dog");
        b.setMastered(null);
        when(wordMasteryMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(a, b));

        DueQueueVO vo = service.due(USER_ID, LANG, null);

        assertThat(vo.today()).isEqualTo(LocalDate.now());
        assertThat(vo.total()).isEqualTo(2);
        assertThat(vo.cards()).extracting(WordCardVO::word).containsExactly("cat", "dog");
        assertThat(vo.cards().get(1).mastered()).isFalse();
    }

    @Test
    @DisplayName("到期队列：未选学 → 403")
    void dueNotEnrolled403() {
        when(enrollmentService.get(USER_ID, LANG)).thenReturn(null);
        assertThatThrownBy(() -> service.due(USER_ID, LANG, 10))
                .isInstanceOfSatisfying(BizException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(403));
    }
}
