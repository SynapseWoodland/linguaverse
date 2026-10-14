package cn.linguaverse.achieve;

/**
 * 8 枚成就定义（静态目录，不入库；解锁记录写 achievement_unlocks）。
 * 评估器在 Task 10 实现，此处只建档。
 */
public enum AchievementCatalog {
    FIRST_STEP("first_step", "初学乍练", "完成第一节课", "seedling"),
    STREAK_3("streak_3", "三日不辍", "连续学习 3 天", "flame"),
    STREAK_7("streak_7", "一周坚持", "连续学习 7 天", "sun"),
    LESSONS_10("lessons_10", "十课达人", "累计完成 10 节课", "book"),
    WORDS_50("words_50", "词汇新星", "累计掌握 50 个单词", "cards"),
    PERFECT("perfect", "满分通关", "单节课正确率达到 100%", "star"),
    POLYGLOT("polyglot", "三语行者", "加入 3 个语种的学习", "globe"),
    SOCIAL("social", "社区新星", "在社区发布第一条帖子", "chat");

    private final String code;
    private final String nameZh;
    private final String descZh;
    private final String icon;

    AchievementCatalog(String code, String nameZh, String descZh, String icon) {
        this.code = code;
        this.nameZh = nameZh;
        this.descZh = descZh;
        this.icon = icon;
    }

    public String code() { return code; }
    public String nameZh() { return nameZh; }
    public String descZh() { return descZh; }
    public String icon() { return icon; }
}
