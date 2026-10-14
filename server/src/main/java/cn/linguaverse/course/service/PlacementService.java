package cn.linguaverse.course.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.dto.PlacementViews.PlacementPaperVO;
import cn.linguaverse.course.dto.PlacementViews.PlacementQuestionVO;
import cn.linguaverse.course.dto.PlacementViews.ResultVO;
import cn.linguaverse.course.entity.Language;
import cn.linguaverse.course.entity.PlacementQuestion;
import cn.linguaverse.course.mapper.LanguageMapper;
import cn.linguaverse.course.mapper.PlacementQuestionMapper;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.PlacementResult;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.PlacementResultMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlacementService {

    private final LanguageMapper languageMapper;
    private final PlacementQuestionMapper questionMapper;
    private final PlacementResultMapper resultMapper;
    private final ActivityMapper activityMapper;
    private final EnrollmentService enrollmentService;
    private final ObjectMapper objectMapper;

    public PlacementService(LanguageMapper languageMapper, PlacementQuestionMapper questionMapper,
                            PlacementResultMapper resultMapper, ActivityMapper activityMapper,
                            EnrollmentService enrollmentService, ObjectMapper objectMapper) {
        this.languageMapper = languageMapper;
        this.questionMapper = questionMapper;
        this.resultMapper = resultMapper;
        this.activityMapper = activityMapper;
        this.enrollmentService = enrollmentService;
        this.objectMapper = objectMapper;
    }

    /** 下发定级试卷：只含题干/选项，绝不含 answer_index。 */
    public PlacementPaperVO paper(String langCode) {
        requireLang(langCode);
        List<PlacementQuestion> questions = questionMapper.selectList(
                new QueryWrapper<PlacementQuestion>().eq("lang_code", langCode).orderByAsc("sort_no"));
        if (questions.isEmpty()) {
            throw BizException.notFound("该语种暂无定级题");
        }
        List<PlacementQuestionVO> vos = questions.stream()
                .map(q -> new PlacementQuestionVO(q.getId(), q.getSortNo(), q.getStem(),
                        readOptions(q.getOptionsJson())))
                .toList();
        return new PlacementPaperVO(langCode, vos.size(), vos);
    }

    /**
     * 交卷判分：答案按 sort_no 顺序提交。
     * 0-3 正确 → L1；4-6 → L2；7-10 → L3（内容仅发布 L1-L3，封顶 3；全错也保底 L1）。
     */
    @Transactional
    public ResultVO submit(long userId, String langCode, List<Integer> answers) {
        requireLang(langCode);
        List<PlacementQuestion> questions = questionMapper.selectList(
                new QueryWrapper<PlacementQuestion>().eq("lang_code", langCode).orderByAsc("sort_no"));
        if (questions.isEmpty()) {
            throw BizException.notFound("该语种暂无定级题");
        }
        if (answers == null || answers.size() != questions.size()) {
            throw BizException.badRequest("答案数量应为 " + questions.size() + " 题");
        }
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer choice = answers.get(i);
            if (choice == null) {
                throw BizException.badRequest("第 " + (i + 1) + " 题缺少答案");
            }
            if (choice < 0 || choice >= 4) {
                throw BizException.badRequest("第 " + (i + 1) + " 题选项下标非法");
            }
            if (choice.equals(questions.get(i).getAnswerIndex())) {
                correct++;
            }
        }

        int suggestedLevel = suggestLevel(correct, questions.size());

        PlacementResult result = new PlacementResult();
        result.setUserId(userId);
        result.setLangCode(langCode);
        result.setCorrectCount(correct);
        result.setTotal(questions.size());
        result.setSuggestedLevel(suggestedLevel);
        resultMapper.insert(result);

        // 定级完成即加入该语种，并把建议级别写回 enrollment
        enrollmentService.upsert(userId, langCode, suggestedLevel);

        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setType("PLACEMENT");
        activity.setRefId(result.getId());
        activity.setXp(0);
        activity.setScore(Math.round(correct * 100f / questions.size()));
        activityMapper.insert(activity);

        return new ResultVO(langCode, correct, questions.size(), suggestedLevel, true);
    }

    private int suggestLevel(int correct, int total) {
        double ratio = (double) correct / total;
        if (ratio >= 0.7) {
            return 3;
        }
        if (ratio >= 0.4) {
            return 2;
        }
        return 1;
    }

    private Language requireLang(String langCode) {
        Language lang = languageMapper.selectById(langCode);
        if (lang == null) {
            throw BizException.notFound("语种不存在: " + langCode);
        }
        return lang;
    }

    private List<String> readOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("定级选项 JSON 解析失败", e);
        }
    }
}
