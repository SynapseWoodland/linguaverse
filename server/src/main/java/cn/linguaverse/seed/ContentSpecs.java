package cn.linguaverse.seed;

import java.util.List;

/** 语料结构定义。 */
public final class ContentSpecs {
    private ContentSpecs() {}

    public record Vocab(String word, String phonetic, String zh, String ex, String exZh) {}
    public record GrammarQ(String stem, List<String> options, int answer, String explain) {}
    public record Line(String speaker, String text, String zh) {}
    public record Listening(String title, List<Line> lines, String stem, List<String> options,
                            int answer, String explain) {}
    public record Speaking(String text, String zh) {}
    public record Lesson(String slug, String title, String nativeTitle, String scene,
                         List<Vocab> vocab, String grammarTitle, String grammarBody,
                         List<GrammarQ> grammar, Listening listening, List<Speaking> speaking) {}
    public record Unit(String title, String nativeTitle, List<Lesson> lessons) {}
    public record Level(int no, String title, List<Unit> units) {}
    public record PlacementQ(String stem, List<String> options, int answer) {}

    static Vocab v(String word, String phonetic, String zh, String ex, String exZh) {
        return new Vocab(word, phonetic, zh, ex, exZh);
    }

    static GrammarQ q(String stem, List<String> options, int answer, String explain) {
        return new GrammarQ(stem, options, answer, explain);
    }

    static Line l(String speaker, String text, String zh) {
        return new Line(speaker, text, zh);
    }

    static Speaking s(String text, String zh) {
        return new Speaking(text, zh);
    }
}
