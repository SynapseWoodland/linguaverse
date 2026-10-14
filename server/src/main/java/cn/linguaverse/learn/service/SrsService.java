package cn.linguaverse.learn.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.entity.Lesson;
import cn.linguaverse.course.entity.LessonVocab;
import cn.linguaverse.course.mapper.LessonMapper;
import cn.linguaverse.course.mapper.LessonVocabMapper;
import cn.linguaverse.learn.dto.ReviewViews.DueQueueVO;
import cn.linguaverse.learn.dto.ReviewViews.WordCardVO;
import cn.linguaverse.learn.entity.WordMastery;
import cn.linguaverse.learn.mapper.WordMasteryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SRS-lite：
 * UNKNOWN（不认识）→ stage 归零，当天再次到期
 * VAGUE（模糊）  → stage +1，明天到期
 * KNOWN（认识）  → stage +2，3 天后到期；stage ≥ 6 视为掌握
 */
@Service
public class SrsService {

    public static final int MASTER_STAGE = 6;

    private final WordMasteryMapper wordMasteryMapper;
    private final LessonVocabMapper lessonVocabMapper;
    private final LessonMapper lessonMapper;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final AchievementService achievementService;

    public SrsService(WordMasteryMapper wordMasteryMapper, LessonVocabMapper lessonVocabMapper,
                      LessonMapper lessonMapper, EnrollmentService enrollmentService,
                      ProgressService progressService, AchievementService achievementService) {
        this.wordMasteryMapper = wordMasteryMapper;
        this.lessonVocabMapper = lessonVocabMapper;
        this.lessonMapper = lessonMapper;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.achievementService = achievementService;
    }

    public DueQueueVO due(long userId, String langCode, Integer limit) {
        if (enrollmentService.get(userId, langCode) == null) {
            throw BizException.forbidden("请先加入该语种的学习");
        }
        int max = (limit == null || limit <= 0) ? 50 : Math.min(limit, 100);
        LocalDate today = LocalDate.now();
        List<WordMastery> rows = wordMasteryMapper.selectList(new QueryWrapper<WordMastery>()
                .eq("user_id", userId)
                .eq("lang_code", langCode)
                .eq("mastered", false)
                .le("due_date", today)
                .orderByAsc("due_date", "stage", "id")
                .last("LIMIT " + max));
        List<WordCardVO> cards = rows.stream().map(this::toCard).toList();
        return new DueQueueVO(today, cards.size(), cards);
    }

    @Transactional
    public WordCardVO rate(long userId, String langCode, String word, String rating) {
        if (enrollmentService.get(userId, langCode) == null) {
            throw BizException.forbidden("请先加入该语种的学习");
        }
        // 单词必须来自该语种已解锁课时的课程词表
        LessonVocab source = lessonVocabMapper.findInLang(langCode, word);
        if (source == null) {
            throw BizException.notFound("该语种词表中不存在这个单词");
        }
        Lesson sourceLesson = lessonMapper.selectById(source.getLessonId());
        if (sourceLesson == null || !progressService.isUnlocked(userId, sourceLesson)) {
            throw BizException.forbidden("请先解锁该单词所在的课时");
        }

        LocalDate today = LocalDate.now();
        WordMastery m = wordMasteryMapper.selectOne(new QueryWrapper<WordMastery>()
                .eq("user_id", userId).eq("lang_code", langCode).eq("word", word));
        boolean isNew = m == null;
        if (isNew) {
            m = new WordMastery();
            m.setUserId(userId);
            m.setLangCode(langCode);
            m.setWord(word);
            m.setPhonetic(source.getPhonetic());
            m.setMeaningZh(source.getMeaningZh());
            m.setStage(0);
            m.setDueDate(today);
            m.setMastered(false);
            m.setCreatedAt(LocalDateTime.now());
        }

        LocalDate due;
        switch (rating) {
            case "UNKNOWN" -> {
                m.setStage(0);
                m.setMastered(false);
                due = today;
            }
            case "VAGUE" -> {
                m.setStage(m.getStage() + 1);
                due = today.plusDays(1);
            }
            default -> { // KNOWN
                m.setStage(m.getStage() + 2);
                due = today.plusDays(3);
                if (m.getStage() >= MASTER_STAGE) {
                    m.setMastered(true);
                }
            }
        }
        m.setLastRating(rating);
        m.setDueDate(due);
        m.setUpdatedAt(LocalDateTime.now());

        boolean becameMastered = Boolean.TRUE.equals(m.getMastered());
        if (isNew) {
            wordMasteryMapper.insert(m);
        } else {
            wordMasteryMapper.updateById(m);
        }
        if (becameMastered) {
            achievementService.evaluateOnWordMastered(userId);
        }
        return toCard(m);
    }

    private WordCardVO toCard(WordMastery m) {
        return new WordCardVO(m.getId(), m.getLangCode(), m.getWord(), m.getPhonetic(),
                m.getMeaningZh(), m.getStage(), m.getDueDate(), m.getLastRating(),
                Boolean.TRUE.equals(m.getMastered()));
    }
}
