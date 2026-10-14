package cn.linguaverse.course.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.course.dto.QuizViews.*;
import cn.linguaverse.course.entity.*;
import cn.linguaverse.course.mapper.*;
import cn.linguaverse.learn.entity.Activity;
import cn.linguaverse.learn.entity.LessonStageProgress;
import cn.linguaverse.learn.mapper.ActivityMapper;
import cn.linguaverse.learn.mapper.LessonStageProgressMapper;
import cn.linguaverse.learn.service.EnrollmentService;
import cn.linguaverse.learn.service.ProgressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Task 6：语法/听力/口语判题。
 * 答案只在判题响应中返回（用户已作答），课时详情 GET 永不下发。
 * 每次提交写 activities 流水（练习可重试，保留历史），并 upsert 对应环节进度。
 */
@Service
public class QuizService {

    private final LessonMapper lessonMapper;
    private final GrammarQuestionMapper grammarQuestionMapper;
    private final ListeningMaterialMapper listeningMapper;
    private final SpeakingSentenceMapper speakingMapper;
    private final ActivityMapper activityMapper;
    private final LessonStageProgressMapper stageProgressMapper;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper;

    public QuizService(LessonMapper lessonMapper, GrammarQuestionMapper grammarQuestionMapper,
                       ListeningMaterialMapper listeningMapper, SpeakingSentenceMapper speakingMapper,
                       ActivityMapper activityMapper, LessonStageProgressMapper stageProgressMapper,
                       EnrollmentService enrollmentService, ProgressService progressService,
                       ObjectMapper objectMapper) {
        this.lessonMapper = lessonMapper;
        this.grammarQuestionMapper = grammarQuestionMapper;
        this.listeningMapper = listeningMapper;
        this.speakingMapper = speakingMapper;
        this.activityMapper = activityMapper;
        this.stageProgressMapper = stageProgressMapper;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.objectMapper = objectMapper;
    }

    // ---------- 语法 ----------

    @Transactional
    public GrammarResultVO gradeGrammar(long userId, long lessonId, List<GrammarAnswerItem> answers) {
        Lesson lesson = requireAccessibleLesson(userId, lessonId);
        List<GrammarQuestion> questions = grammarQuestionMapper.selectList(
                new QueryWrapper<GrammarQuestion>().eq("lesson_id", lessonId).orderByAsc("sort_no"));
        if (questions.isEmpty()) {
            throw BizException.badRequest("本课时没有语法题");
        }
        Map<Long, GrammarQuestion> byId = new HashMap<>();
        for (GrammarQuestion q : questions) {
            byId.put(q.getId(), q);
        }
        if (answers.size() != questions.size()) {
            throw BizException.badRequest("作答题数应为 " + questions.size() + " 题");
        }
        Set<Long> seen = new HashSet<>();
        List<GrammarItemResult> results = new ArrayList<>();
        int correctCount = 0;
        for (GrammarAnswerItem ans : answers) {
            GrammarQuestion q = byId.get(ans.questionId());
            if (q == null || !seen.add(ans.questionId())) {
                throw BizException.badRequest("题目不属于本课时或重复作答: " + ans.questionId());
            }
            int optionCount = parseOptions(q.getOptionsJson()).size();
            if (ans.choiceIndex() < 0 || ans.choiceIndex() >= optionCount) {
                throw BizException.badRequest("选项下标非法: " + ans.questionId());
            }
            boolean correct = ans.choiceIndex().equals(q.getAnswerIndex());
            if (correct) {
                correctCount++;
            }
            results.add(new GrammarItemResult(q.getId(), ans.choiceIndex(), correct,
                    q.getAnswerIndex(), q.getExplanation()));
        }
        results.sort(Comparator.comparingLong(GrammarItemResult::questionId));
        int score = Math.round(correctCount * 100f / questions.size());
        saveActivity(userId, "GRAMMAR", lessonId, score,
                Map.of("correctCount", correctCount, "total", questions.size()));
        saveStage(userId, lessonId, "GRAMMAR", score);
        return new GrammarResultVO(lessonId, correctCount, questions.size(), score, results);
    }

    // ---------- 听力 ----------

    @Transactional
    public ListeningResultVO gradeListening(long userId, long lessonId, int choiceIndex) {
        Lesson lesson = requireAccessibleLesson(userId, lessonId);
        ListeningMaterial m = listeningMapper.selectOne(
                new QueryWrapper<ListeningMaterial>().eq("lesson_id", lessonId));
        if (m == null) {
            throw BizException.badRequest("本课时没有听力材料");
        }
        int optionCount = parseOptions(m.getOptionsJson()).size();
        if (choiceIndex < 0 || choiceIndex >= optionCount) {
            throw BizException.badRequest("选项下标非法");
        }
        boolean correct = choiceIndex == m.getAnswerIndex();
        int score = correct ? 100 : 0;
        saveActivity(userId, "LISTENING", lessonId, score,
                Map.of("correct", correct, "listeningId", m.getId()));
        saveStage(userId, lessonId, "LISTENING", score);
        return new ListeningResultVO(lessonId, correct, choiceIndex,
                m.getAnswerIndex(), score, m.getExplanation());
    }

    // ---------- 口语 ----------

    @Transactional
    public SpeakingResultVO gradeSpeaking(long userId, long lessonId, SpeakingSubmitRequest req) {
        Lesson lesson = requireAccessibleLesson(userId, lessonId);
        List<SpeakingSentence> sentences = speakingMapper.selectList(
                new QueryWrapper<SpeakingSentence>().eq("lesson_id", lessonId).orderByAsc("sort_no"));
        if (sentences.isEmpty()) {
            throw BizException.badRequest("本课时没有跟读句");
        }
        Map<Long, SpeakingSentence> byId = new HashMap<>();
        for (SpeakingSentence s : sentences) {
            byId.put(s.getId(), s);
        }
        if (req.sentences().size() != sentences.size()) {
            throw BizException.badRequest("跟读句数应为 " + sentences.size() + " 句");
        }

        Set<Long> seen = new HashSet<>();
        List<SpeakingSentenceResult> results = new ArrayList<>();
        int total = 0;
        for (SpeakingSentenceItem item : req.sentences()) {
            SpeakingSentence s = byId.get(item.sentenceId());
            if (s == null || !seen.add(item.sentenceId())) {
                throw BizException.badRequest("句子不属于本课时或重复提交: " + item.sentenceId());
            }
            Integer sentenceScore;
            if ("RECOGNITION".equals(req.mode())) {
                String transcript = item.transcript() == null ? "" : item.transcript().trim();
                if (!transcript.isEmpty()) {
                    // 服务端按字符相似度对识别文本判分，避免完全信任客户端
                    sentenceScore = similarityScore(s.getText(), transcript);
                } else if (item.score() != null) {
                    sentenceScore = item.score();
                } else {
                    throw BizException.badRequest("识别模式下缺少识别文本或分数: " + item.sentenceId());
                }
            } else { // SELF 三档自评
                if (item.rating() == null) {
                    throw BizException.badRequest("自评模式下缺少自评档位: " + item.sentenceId());
                }
                sentenceScore = switch (item.rating()) {
                    case "NEED_PRACTICE" -> 60;
                    case "OK" -> 80;
                    default -> 95; // FLUENT
                };
            }
            total += sentenceScore;
            results.add(new SpeakingSentenceResult(s.getId(), sentenceScore, feedback(sentenceScore)));
        }
        results.sort(Comparator.comparingLong(SpeakingSentenceResult::sentenceId));
        int score = Math.round(total / (float) sentences.size());
        saveActivity(userId, "SPEAKING", lessonId, score,
                Map.of("mode", req.mode(), "sentenceCount", sentences.size()));
        saveStage(userId, lessonId, "SPEAKING", score);
        return new SpeakingResultVO(lessonId, req.mode(), score, results);
    }

    // ---------- 内部工具 ----------

    private Lesson requireAccessibleLesson(long userId, long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || !Boolean.TRUE.equals(lesson.getReleased())) {
            throw BizException.notFound("课时不存在");
        }
        if (enrollmentService.get(userId, lesson.getLangCode()) == null) {
            throw BizException.forbidden("请先加入该语种的学习");
        }
        if (!progressService.isUnlocked(userId, lesson)) {
            throw BizException.forbidden("请先完成上一节课再解锁本课");
        }
        return lesson;
    }

    private void saveActivity(long userId, String type, long lessonId, int score, Map<String, Object> payload) {
        Activity a = new Activity();
        a.setUserId(userId);
        a.setType(type);
        a.setRefId(lessonId);
        a.setXp(0); // 练习过程不发 XP，XP 在课时结算时统一发放（Task 7）
        a.setScore(score);
        try {
            a.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("流水 payload 序列化失败", e);
        }
        a.setCreatedAt(LocalDateTime.now());
        activityMapper.insert(a);
    }

    private void saveStage(long userId, long lessonId, String stage, int score) {
        LessonStageProgress row = stageProgressMapper.selectOne(new QueryWrapper<LessonStageProgress>()
                .eq("user_id", userId).eq("lesson_id", lessonId).eq("stage", stage));
        LocalDateTime now = LocalDateTime.now();
        if (row == null) {
            row = new LessonStageProgress();
            row.setUserId(userId);
            row.setLessonId(lessonId);
            row.setStage(stage);
            row.setScore(score);
            row.setUpdatedAt(now);
            stageProgressMapper.insert(row);
        } else {
            row.setScore(score);
            row.setUpdatedAt(now);
            stageProgressMapper.updateById(row);
        }
    }

    private List<String> parseOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("选项 JSON 解析失败", e);
        }
    }

    /** 归一化后按字符 Levenshtein 计算 0–100 相似度，兼容英/日/韩文本。 */
    static int similarityScore(String canonical, String recognized) {
        String a = normalize(canonical);
        String b = normalize(recognized);
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        int dist = levenshtein(a, b);
        double sim = 1.0 - (dist / (double) Math.max(a.length(), b.length()));
        return Math.max(0, Math.min(100, (int) Math.round(sim * 100)));
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Punct}\\s，。、！？「」『』（）〜～·…—\\-]", "");
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            // 交换数组引用继续下一轮
            prev = cur;
            cur = tmp;
        }
        return prev[b.length()];
    }

    private static String feedback(int score) {
        if (score >= 85) {
            return "发音很流利，继续保持";
        }
        if (score >= 60) {
            return "读得不错，注意个别发音";
        }
        return "建议再听示范并跟读一遍";
    }
}
