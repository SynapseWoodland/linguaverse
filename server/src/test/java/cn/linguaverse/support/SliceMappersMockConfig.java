package cn.linguaverse.support;

import cn.linguaverse.achieve.mapper.AchievementUnlockMapper;
import cn.linguaverse.community.mapper.CommentMapper;
import cn.linguaverse.community.mapper.PostLikeMapper;
import cn.linguaverse.community.mapper.PostMapper;
import cn.linguaverse.course.mapper.GrammarPointMapper;
import cn.linguaverse.course.mapper.GrammarQuestionMapper;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.course.mapper.LevelMapper;
import cn.linguaverse.course.mapper.ListeningMaterialMapper;
import cn.linguaverse.course.mapper.PlacementQuestionMapper;
import cn.linguaverse.course.mapper.SpeakingSentenceMapper;
import cn.linguaverse.course.mapper.UnitMapper;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.EnrollmentMapper;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.learn.mapper.PlacementResultMapper;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import cn.linguaverse.user.mapper.UserMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

/**
 * @WebMvcTest 切片不装配 MyBatis（无 SqlSessionFactory），但主类 @MapperScan 仍会注册全部 Mapper
 * 导致切片上下文启动失败。这里以 Mockito mock 顶替全部 21 个 Mapper（仅用于让上下文装配）。
 */
@TestConfiguration
public class SliceMappersMockConfig {

    @Bean LanguageMapper languageMapper() { return mock(LanguageMapper.class); }
    @Bean LevelMapper levelMapper() { return mock(LevelMapper.class); }
    @Bean UnitMapper unitMapper() { return mock(UnitMapper.class); }
    @Bean LessonMapper lessonMapper() { return mock(LessonMapper.class); }
    @Bean LessonVocabMapper lessonVocabMapper() { return mock(LessonVocabMapper.class); }
    @Bean GrammarPointMapper grammarPointMapper() { return mock(GrammarPointMapper.class); }
    @Bean GrammarQuestionMapper grammarQuestionMapper() { return mock(GrammarQuestionMapper.class); }
    @Bean ListeningMaterialMapper listeningMaterialMapper() { return mock(ListeningMaterialMapper.class); }
    @Bean SpeakingSentenceMapper speakingSentenceMapper() { return mock(SpeakingSentenceMapper.class); }
    @Bean PlacementQuestionMapper placementQuestionMapper() { return mock(PlacementQuestionMapper.class); }
    @Bean EnrollmentMapper enrollmentMapper() { return mock(EnrollmentMapper.class); }
    @Bean PlacementResultMapper placementResultMapper() { return mock(PlacementResultMapper.class); }
    @Bean WordMasteryMapper wordMasteryMapper() { return mock(WordMasteryMapper.class); }
    @Bean LessonCompletionMapper lessonCompletionMapper() { return mock(LessonCompletionMapper.class); }
    @Bean LessonStageProgressMapper lessonStageProgressMapper() { return mock(LessonStageProgressMapper.class); }
    @Bean ActivityMapper activityMapper() { return mock(ActivityMapper.class); }
    @Bean UserMapper userMapper() { return mock(UserMapper.class); }
    @Bean PostMapper postMapper() { return mock(PostMapper.class); }
    @Bean CommentMapper commentMapper() { return mock(CommentMapper.class); }
    @Bean PostLikeMapper postLikeMapper() { return mock(PostLikeMapper.class); }
    @Bean AchievementUnlockMapper achievementUnlockMapper() { return mock(AchievementUnlockMapper.class); }
}
