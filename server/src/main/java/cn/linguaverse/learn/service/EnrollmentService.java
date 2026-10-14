package cn.linguaverse.learn.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.entity.Language;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.learn.entity.Enrollment;
import cn.linguaverse.learn.mapper.EnrollmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final LanguageMapper languageMapper;
    private final AchievementService achievementService;

    public EnrollmentService(EnrollmentMapper enrollmentMapper, LanguageMapper languageMapper,
                             AchievementService achievementService) {
        this.enrollmentMapper = enrollmentMapper;
        this.languageMapper = languageMapper;
        this.achievementService = achievementService;
    }

    public List<Enrollment> listByUser(long userId) {
        return enrollmentMapper.selectList(
                new QueryWrapper<Enrollment>().eq("user_id", userId).orderByAsc("started_at"));
    }

    public Enrollment get(long userId, String langCode) {
        return enrollmentMapper.selectOne(new QueryWrapper<Enrollment>()
                .eq("user_id", userId).eq("lang_code", langCode));
    }

    /**
     * 幂等加入语种。placementLevel 为 null 时不覆盖已有定级（用于 PLACEMENT 先占位）。
     */
    public Enrollment upsert(long userId, String langCode, Integer placementLevel) {
        Language lang = languageMapper.selectById(langCode);
        if (lang == null) {
            throw BizException.badRequest("不支持的语种: " + langCode);
        }
        Enrollment existing = get(userId, langCode);
        if (existing != null) {
            if (placementLevel != null && !placementLevel.equals(existing.getPlacementLevel())) {
                existing.setPlacementLevel(placementLevel);
                enrollmentMapper.updateById(existing);
            }
            return existing;
        }
        Enrollment e = new Enrollment();
        e.setUserId(userId);
        e.setLangCode(langCode);
        e.setPlacementLevel(placementLevel);
        enrollmentMapper.insert(e);
        achievementService.evaluateOnEnrollment(userId);
        return e;
    }
}
