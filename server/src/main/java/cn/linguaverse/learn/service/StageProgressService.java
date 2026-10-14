package cn.linguaverse.learn.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.learn.dto.StageProgressViews.ProgressResponse;
import cn.linguaverse.learn.dto.StageProgressViews.StageVO;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** 课时四环节进度上报：退出后可从课时详情 completedStages 恢复。 */
@Service
public class StageProgressService {

    private final LessonMapper lessonMapper;
    private final LessonStageProgressMapper progressMapper;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;

    public StageProgressService(LessonMapper lessonMapper, LessonStageProgressMapper progressMapper,
                                EnrollmentService enrollmentService, ProgressService progressService) {
        this.lessonMapper = lessonMapper;
        this.progressMapper = progressMapper;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
    }

    public ProgressResponse save(long userId, long lessonId, String stage, Integer score) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || !Boolean.TRUE.equals(lesson.getReleased())) {
            throw BizException.notFound("课时不存在");
        }
        Enrollment enrollment = enrollmentService.get(userId, lesson.getLangCode());
        if (enrollment == null) {
            throw BizException.forbidden("请先加入该语种的学习");
        }
        if (!progressService.isUnlocked(userId, lesson)) {
            throw BizException.forbidden("请先完成上一节课再解锁本课");
        }

        LessonStageProgress row = progressMapper.selectOne(new QueryWrapper<LessonStageProgress>()
                .eq("user_id", userId).eq("lesson_id", lessonId).eq("stage", stage));
        LocalDateTime now = LocalDateTime.now();
        if (row == null) {
            row = new LessonStageProgress();
            row.setUserId(userId);
            row.setLessonId(lessonId);
            row.setStage(stage);
            row.setScore(score);
            row.setUpdatedAt(now);
            progressMapper.insert(row);
        } else {
            row.setScore(score);
            row.setUpdatedAt(now);
            progressMapper.updateById(row);
        }
        return new ProgressResponse(lessonId, listStages(userId, lessonId));
    }

    public List<StageVO> listStages(long userId, long lessonId) {
        return progressMapper.selectList(new QueryWrapper<LessonStageProgress>()
                        .eq("user_id", userId).eq("lesson_id", lessonId).orderByAsc("stage"))
                .stream().map(p -> new StageVO(p.getStage(), p.getScore())).toList();
    }
}
