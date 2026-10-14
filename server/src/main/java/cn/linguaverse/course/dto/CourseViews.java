package cn.linguaverse.course.dto;

import java.util.List;

/** 课程树与课时详情响应视图。任何题目答案字段都不得出现在这里。 */
public final class CourseViews {
    private CourseViews() {}

    public record LessonNodeVO(long id, int lessonNo, int seqNo, String slug, String title,
                               String titleNative, boolean completed, boolean unlocked) {}

    public record UnitNodeVO(int unitNo, String title, String titleNative, List<LessonNodeVO> lessons) {}

    public record LevelNodeVO(int levelNo, String title, boolean released, List<UnitNodeVO> units) {}

    public record LanguageVO(String code, String nameZh, String nameNative, String color,
                             Integer sortNo, boolean enrolled, Integer placementLevel,
                             List<LevelNodeVO> levels) {}

    // ---------- 课时详情 ----------

    public record VocabVO(String word, String phonetic, String meaningZh, String example, String exampleZh) {}

    public record QuestionVO(long id, int sortNo, String stem, List<String> options) {}

    public record GrammarVO(String title, String explanation, List<QuestionVO> questions) {}

    public record LineVO(String speaker, String text, String zh) {}

    public record ListeningVO(long id, String title, List<LineVO> lines,
                              String questionStem, List<String> options) {}

    public record SpeakingVO(long id, int sortNo, String text, String textZh) {}

    public record LessonDetailVO(long id, String langCode, int seqNo, String slug,
                                 String title, String titleNative, String scene,
                                 boolean unlocked, boolean completed,
                                 List<VocabVO> vocab, GrammarVO grammar,
                                 ListeningVO listening, List<SpeakingVO> speaking,
                                 List<String> completedStages) {}
}
