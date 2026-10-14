package cn.linguaverse.achieve.service;

import cn.linguaverse.achieve.dto.AchievementVO;
import cn.linguaverse.achieve.entity.AchievementUnlock;
import cn.linguaverse.achieve.mapper.AchievementUnlockMapper;
import cn.linguaverse.community.entity.Post;
import cn.linguaverse.community.mapper.PostMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.EnrollmentMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import cn.linguaverse.user.User;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** Task 10：GET /achievements 成就墙——8 枚目录顺序、实时进度映射、current 上限收敛、解锁时间。 */
@ExtendWith(MockitoExtension.class)
class AchievementQueryServiceTest {

    private static final long USER_ID = 9L;

    @Mock
    AchievementUnlockMapper unlockMapper;
    @Mock
    LessonCompletionMapper completionMapper;
    @Mock
    WordMasteryMapper wordMasteryMapper;
    @Mock
    EnrollmentMapper enrollmentMapper;
    @Mock
    PostMapper postMapper;
    @Mock
    UserMapper userMapper;

    AchievementQueryService service;

    @BeforeEach
    void setUp() {
        service = new AchievementQueryService(unlockMapper, completionMapper, wordMasteryMapper,
                enrollmentMapper, postMapper, userMapper);
    }

    private void stubCounts(long lessons, long perfect, long mastered, long langs, long posts) {
        when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(lessons, perfect);
        when(wordMasteryMapper.selectCount(any(QueryWrapper.class))).thenReturn(mastered);
        when(enrollmentMapper.selectCount(any(QueryWrapper.class))).thenReturn(langs);
        when(postMapper.selectCount(any(QueryWrapper.class))).thenReturn(posts);
    }

    private Map<String, AchievementVO> byCode(List<AchievementVO> list) {
        return list.stream().collect(Collectors.toMap(AchievementVO::code, Function.identity()));
    }

    @Test
    @DisplayName("新用户：8 枚全锁、进度 0、target 与目录一致")
    void newUserAllLocked() {
        when(unlockMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());
        stubCounts(0, 0, 0, 0, 0);
        User u = new User();
        u.setId(USER_ID);
        u.setStreak(null); // streak 未初始化 → 按 0 处理
        when(userMapper.selectById(USER_ID)).thenReturn(u);

        List<AchievementVO> list = service.list(USER_ID);

        assertThat(list).extracting(AchievementVO::code).containsExactly(
                "first_step", "streak_3", "streak_7", "lessons_10",
                "words_50", "perfect", "polyglot", "social");
        assertThat(list).allSatisfy(vo -> {
            assertThat(vo.unlocked()).isFalse();
            assertThat(vo.unlockedAt()).isNull();
            assertThat(vo.current()).isZero();
        });
        assertThat(list).extracting(AchievementVO::target)
                .containsExactly(1L, 3L, 7L, 10L, 50L, 1L, 3L, 1L);
        assertThat(list.get(0).nameZh()).isEqualTo("初学乍练");
        assertThat(list.get(0).icon()).isEqualTo("seedling");
    }

    @Test
    @DisplayName("老用户：进度映射正确，current 超出 target 时收敛为 target")
    void progressMappingAndClamp() {
        LocalDateTime firstStepAt = LocalDateTime.now().minusDays(3);
        AchievementUnlock firstStep = new AchievementUnlock();
        firstStep.setUserId(USER_ID);
        firstStep.setCode("first_step");
        firstStep.setUnlockedAt(firstStepAt);
        AchievementUnlock social = new AchievementUnlock();
        social.setUserId(USER_ID);
        social.setCode("social");
        social.setUnlockedAt(LocalDateTime.now());
        when(unlockMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(firstStep, social));

        stubCounts(5, 1, 60, 3, 2); // 完成 5 课、1 次满分、掌握 60 词、3 语种、2 帖
        User u = new User();
        u.setId(USER_ID);
        u.setStreak(3);
        when(userMapper.selectById(USER_ID)).thenReturn(u);

        Map<String, AchievementVO> map = byCode(service.list(USER_ID));

        assertThat(map.get("first_step").unlocked()).isTrue();
        assertThat(map.get("first_step").unlockedAt()).isEqualTo(firstStepAt);
        assertThat(map.get("first_step").current()).isEqualTo(1); // min(5,1)

        assertThat(map.get("lessons_10").current()).isEqualTo(5);
        assertThat(map.get("words_50").current()).isEqualTo(50); // min(60,50)
        assertThat(map.get("polyglot").current()).isEqualTo(3);

        assertThat(map.get("streak_3").current()).isEqualTo(3);
        assertThat(map.get("streak_7").current()).isEqualTo(3); // min(3,7)

        assertThat(map.get("social").current()).isEqualTo(1); // min(2,1)
        assertThat(map.get("social").unlocked()).isTrue();
        assertThat(map.get("perfect").current()).isEqualTo(1);
    }

    @Test
    @DisplayName("用户不存在或 streak 缺失 → streak 指标按 0")
    void streakDefaultsToZero() {
        when(unlockMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());
        stubCounts(1, 0, 0, 1, 0);
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        Map<String, AchievementVO> map = byCode(service.list(USER_ID));

        assertThat(map.get("streak_3").current()).isZero();
        assertThat(map.get("streak_7").current()).isZero();
        assertThat(map.get("first_step").current()).isEqualTo(1);
    }

    @Test
    @DisplayName("解锁记录重复 code 时去重取首条，不抛异常")
    void duplicateUnlockCodesDeduped() {
        AchievementUnlock a = new AchievementUnlock();
        a.setCode("social");
        a.setUnlockedAt(LocalDateTime.now().minusDays(1));
        AchievementUnlock b = new AchievementUnlock();
        b.setCode("social");
        b.setUnlockedAt(LocalDateTime.now());
        when(unlockMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(a, b));

        lenient().when(completionMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        lenient().when(wordMasteryMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        lenient().when(enrollmentMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        lenient().when(postMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        lenient().when(userMapper.selectById(USER_ID)).thenReturn(null);

        List<AchievementVO> list = service.list(USER_ID);

        assertThat(list).hasSize(8);
        assertThat(byCode(list).get("social").unlocked()).isTrue();
    }
}
