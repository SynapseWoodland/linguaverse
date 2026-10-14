package cn.linguaverse.learn.service;

import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.learn.entity.LessonCompletion;
import cn.linguaverse.learn.mapper.LessonCompletionMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 课时进度与解锁规则：语种内严格按 lessons.seq_no 顺序解锁，
 * seq_no=1 默认可学，其余课时需前一课时已完成结算。
 */
@Service
public class ProgressService {

    private final LessonMapper lessonMapper;
    private final LessonCompletionMapper completionMapper;

    public ProgressService(LessonMapper lessonMapper, LessonCompletionMapper completionMapper) {
        this.lessonMapper = lessonMapper;
        this.completionMapper = completionMapper;
    }

    /** 该用户在某语种已完成的课时 id 集合。 */
    public Set<Long> completedLessonIds(long userId, String langCode) {
        List<Lesson> lessons = lessonMapper.selectList(
                new QueryWrapper<Lesson>().eq("lang_code", langCode).select("id"));
        Set<Long> ids = new HashSet<>();
        for (Lesson l : lessons) {
            ids.add(l.getId());
        }
        if (ids.isEmpty()) {
            return ids;
        }
        List<LessonCompletion> done = completionMapper.selectList(
                new QueryWrapper<LessonCompletion>()
                        .eq("user_id", userId)
                        .in("lesson_id", ids)
                        .select("lesson_id"));
        Set<Long> result = new HashSet<>();
        for (LessonCompletion c : done) {
            result.add(c.getLessonId());
        }
        return result;
    }

    public boolean isCompleted(long userId, long lessonId) {
        return completionMapper.selectCount(new QueryWrapper<LessonCompletion>()
                .eq("user_id", userId).eq("lesson_id", lessonId)) > 0;
    }

    /**
     * 全局顺序解锁：seq_no=1 始终解锁；seq_no=n 需要 seq_no=n-1 已完成。
     */
    public boolean isUnlocked(long userId, Lesson lesson) {
        if (lesson.getSeqNo() == null || lesson.getSeqNo() <= 1) {
            return true;
        }
        Lesson prev = lessonMapper.selectOne(new QueryWrapper<Lesson>()
                .eq("lang_code", lesson.getLangCode())
                .eq("seq_no", lesson.getSeqNo() - 1)
                .select("id"));
        return prev != null && isCompleted(userId, prev.getId());
    }

    public long completedCount(long userId, String langCode) {
        return completedLessonIds(userId, langCode).size();
    }
}
