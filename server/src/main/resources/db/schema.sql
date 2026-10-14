-- =============================================================
-- LinguaVerse schema · MySQL 8 · 幂等建表（IF NOT EXISTS）
-- =============================================================
SET NAMES utf8mb4;

-- ---------- 用户 ----------
CREATE TABLE IF NOT EXISTS users (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  username        VARCHAR(32)  NOT NULL COMMENT '登录名 2-16 位',
  password_hash   VARCHAR(100) NOT NULL COMMENT 'BCrypt',
  nickname        VARCHAR(32)  NOT NULL,
  bio             VARCHAR(200) NULL,
  avatar_seed     VARCHAR(16)  NOT NULL DEFAULT 'L',
  role            VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
  daily_goal_xp   INT          NOT NULL DEFAULT 30,
  streak          INT          NOT NULL DEFAULT 0 COMMENT '连续学习天数',
  last_study_date DATE         NULL COMMENT '最近一个计入 streak 的日期',
  onboarded       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已完成入门引导（0/1）',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 迁移：为已存在的 users 表补 onboarded 列（MySQL 8 无 ADD COLUMN IF NOT EXISTS，用信息schema 判定 + 动态 SQL，幂等可重复执行）
SET @has_onboarded := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'onboarded'
);
SET @mig_onboarded := IF(@has_onboarded = 0,
  'ALTER TABLE users ADD COLUMN onboarded TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否已完成入门引导（0/1）'' AFTER last_study_date',
  'DO 0');
PREPARE stmt_mig FROM @mig_onboarded;
EXECUTE stmt_mig;
DEALLOCATE PREPARE stmt_mig;

-- ---------- 课程内容 ----------
CREATE TABLE IF NOT EXISTS languages (
  code       VARCHAR(8)  NOT NULL COMMENT 'en/ja/ko',
  name_zh    VARCHAR(16) NOT NULL,
  name_native VARCHAR(32) NOT NULL,
  color      VARCHAR(16) NOT NULL,
  sort_no    INT         NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语种';

CREATE TABLE IF NOT EXISTS levels (
  id        BIGINT      NOT NULL AUTO_INCREMENT,
  lang_code VARCHAR(8)  NOT NULL,
  level_no  INT         NOT NULL COMMENT '1-6',
  title     VARCHAR(32) NOT NULL,
  released  TINYINT(1)  NOT NULL DEFAULT 0 COMMENT 'L1-L3=1, L4-L6=0',
  PRIMARY KEY (id),
  UNIQUE KEY uk_levels_lang_no (lang_code, level_no),
  CONSTRAINT fk_levels_lang FOREIGN KEY (lang_code) REFERENCES languages(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='级别';

CREATE TABLE IF NOT EXISTS units (
  id        BIGINT      NOT NULL AUTO_INCREMENT,
  level_id  BIGINT      NOT NULL,
  unit_no   INT         NOT NULL COMMENT '级别内 1-2',
  title     VARCHAR(64) NOT NULL,
  title_native VARCHAR(128) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_units_level_no (level_id, unit_no),
  CONSTRAINT fk_units_level FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单元';

CREATE TABLE IF NOT EXISTS lessons (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  unit_id      BIGINT       NOT NULL,
  lesson_no    INT          NOT NULL COMMENT '单元内 1-2',
  lang_code    VARCHAR(8)   NOT NULL COMMENT '冗余，便于查询与解锁',
  seq_no       INT          NOT NULL COMMENT '语种内全局顺序，从 1 开始',
  slug         VARCHAR(64)  NOT NULL,
  title        VARCHAR(64)  NOT NULL COMMENT '中文场景名',
  title_native VARCHAR(128) NOT NULL COMMENT '外文课名',
  scene        VARCHAR(255) NULL COMMENT '场景一句话描述',
  released     TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_lessons_lang_seq (lang_code, seq_no),
  UNIQUE KEY uk_lessons_slug_lang (lang_code, slug),
  CONSTRAINT fk_lessons_unit FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE,
  KEY idx_lessons_lang (lang_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课时（解锁顺序的最小单位）';

CREATE TABLE IF NOT EXISTS lesson_vocab (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  lesson_id  BIGINT       NOT NULL,
  word       VARCHAR(128) NOT NULL,
  phonetic   VARCHAR(64)  NULL,
  meaning_zh VARCHAR(64)  NOT NULL,
  example    VARCHAR(255) NULL,
  example_zh VARCHAR(255) NULL,
  sort_no    INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_vocab_lesson (lesson_id),
  CONSTRAINT fk_vocab_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课时词汇（可下发，非题目答案）';

CREATE TABLE IF NOT EXISTS grammar_points (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  lesson_id     BIGINT       NOT NULL,
  title         VARCHAR(128) NOT NULL,
  explanation   TEXT         NOT NULL COMMENT '讲解正文（可下发）',
  PRIMARY KEY (id),
  KEY idx_gp_lesson (lesson_id),
  CONSTRAINT fk_gp_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语法讲解';

CREATE TABLE IF NOT EXISTS grammar_questions (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  lesson_id    BIGINT       NOT NULL,
  stem         VARCHAR(512) NOT NULL,
  options_json JSON         NOT NULL COMMENT '选项数组',
  answer_index INT          NOT NULL COMMENT '正确项下标，永不下发',
  explanation  VARCHAR(512) NULL,
  sort_no      INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_gq_lesson (lesson_id),
  CONSTRAINT fk_gq_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语法题（服务端判分）';

CREATE TABLE IF NOT EXISTS listening_materials (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  lesson_id     BIGINT       NOT NULL,
  title         VARCHAR(128) NOT NULL,
  lines_json    JSON         NOT NULL COMMENT '逐句 [{speaker,text,zh}]',
  question_stem VARCHAR(512) NOT NULL,
  options_json  JSON         NOT NULL,
  answer_index  INT          NOT NULL COMMENT '永不下发',
  explanation   VARCHAR(512) NULL,
  PRIMARY KEY (id),
  KEY idx_lm_lesson (lesson_id),
  CONSTRAINT fk_lm_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='听力材料（服务端判分）';

CREATE TABLE IF NOT EXISTS speaking_sentences (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  lesson_id BIGINT       NOT NULL,
  text      VARCHAR(255) NOT NULL,
  text_zh   VARCHAR(255) NOT NULL,
  sort_no   INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_ss_lesson (lesson_id),
  CONSTRAINT fk_ss_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='口语跟读句';

CREATE TABLE IF NOT EXISTS placement_questions (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  lang_code    VARCHAR(8)   NOT NULL,
  sort_no      INT          NOT NULL,
  stem         VARCHAR(512) NOT NULL,
  options_json JSON         NOT NULL,
  answer_index INT          NOT NULL COMMENT '永不下发',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pq_lang_sort (lang_code, sort_no),
  CONSTRAINT fk_pq_lang FOREIGN KEY (lang_code) REFERENCES languages(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定级题（服务端判分）';

-- ---------- 学习数据 ----------
CREATE TABLE IF NOT EXISTS enrollments (
  id              BIGINT      NOT NULL AUTO_INCREMENT,
  user_id         BIGINT      NOT NULL,
  lang_code       VARCHAR(8)  NOT NULL,
  placement_level INT         NULL COMMENT '定级建议级别 1-6；直学为 1',
  started_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_enroll_user_lang (user_id, lang_code),
  CONSTRAINT fk_enroll_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_enroll_lang FOREIGN KEY (lang_code) REFERENCES languages(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户选学语种';

CREATE TABLE IF NOT EXISTS placement_results (
  id              BIGINT      NOT NULL AUTO_INCREMENT,
  user_id         BIGINT      NOT NULL,
  lang_code       VARCHAR(8)  NOT NULL,
  correct_count   INT         NOT NULL,
  total           INT         NOT NULL,
  suggested_level INT         NOT NULL,
  created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pr_user_lang (user_id, lang_code),
  CONSTRAINT fk_pr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定级历史';

CREATE TABLE IF NOT EXISTS lesson_completions (
  id            BIGINT   NOT NULL AUTO_INCREMENT,
  user_id       BIGINT   NOT NULL,
  lesson_id     BIGINT   NOT NULL,
  xp            INT      NOT NULL,
  accuracy      INT      NOT NULL COMMENT '四环节平均正确率 0-100',
  words_count   INT      NOT NULL,
  duration_sec  INT      NOT NULL,
  completed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_lc_user_lesson (user_id, lesson_id) COMMENT '幂等：同一用户同一课时只结算一次',
  KEY idx_lc_user_date (user_id, completed_at),
  CONSTRAINT fk_lc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_lc_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课时完成结算（幂等）';

CREATE TABLE IF NOT EXISTS lesson_stage_progress (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  user_id    BIGINT      NOT NULL,
  lesson_id  BIGINT      NOT NULL,
  stage      VARCHAR(16) NOT NULL COMMENT 'VOCAB/GRAMMAR/LISTENING/SPEAKING',
  score      INT         NULL COMMENT '该环节正确率 0-100',
  updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_lsp_user_lesson_stage (user_id, lesson_id, stage),
  KEY idx_lsp_user_lesson (user_id, lesson_id),
  CONSTRAINT fk_lsp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_lsp_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课时四环节进度（退出可恢复）';

CREATE TABLE IF NOT EXISTS word_mastery (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  lang_code     VARCHAR(8)   NOT NULL,
  word          VARCHAR(128) NOT NULL,
  phonetic      VARCHAR(64)  NULL,
  meaning_zh    VARCHAR(64)  NOT NULL,
  stage         INT          NOT NULL DEFAULT 0 COMMENT 'SRS 阶段 0 起',
  due_date      DATE         NOT NULL COMMENT '下次到期日',
  last_rating   VARCHAR(8)   NULL COMMENT 'UNKNOWN/VAGUE/KNOWN',
  mastered      TINYINT(1)   NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_wm_user_lang_word (user_id, lang_code, word),
  KEY idx_wm_due (user_id, due_date),
  CONSTRAINT fk_wm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词记忆（SRS-lite）';

CREATE TABLE IF NOT EXISTS activities (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  type        VARCHAR(24)  NOT NULL COMMENT 'LESSON_DONE/GRAMMAR/LISTENING/SPEAKING/VOCAB_RATE/PLACEMENT',
  ref_id      BIGINT       NULL COMMENT '关联课时等',
  xp          INT          NOT NULL DEFAULT 0,
  score       INT          NULL COMMENT '该次正确率',
  payload_json JSON        NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_act_user_time (user_id, created_at),
  KEY idx_act_user_type (user_id, type),
  CONSTRAINT fk_act_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习流水与 XP 账本';

-- ---------- 社区 ----------
CREATE TABLE IF NOT EXISTS posts (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  author_id   BIGINT       NOT NULL,
  lang_code   VARCHAR(8)   NULL,
  title       VARCHAR(128) NULL,
  content     VARCHAR(2000) NOT NULL,
  like_count  INT          NOT NULL DEFAULT 0,
  comment_count INT        NOT NULL DEFAULT 0,
  hidden      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '机审/管理员隐藏',
  deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '作者删除（逻辑删除）',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_posts_feed (deleted, hidden, created_at),
  CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子';

CREATE TABLE IF NOT EXISTS comments (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  post_id    BIGINT       NOT NULL,
  author_id  BIGINT       NOT NULL,
  content    VARCHAR(1000) NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_comments_post (post_id),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论（级联删除）';

CREATE TABLE IF NOT EXISTS post_likes (
  id         BIGINT   NOT NULL AUTO_INCREMENT,
  post_id    BIGINT   NOT NULL,
  user_id    BIGINT   NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_like_post_user (post_id, user_id),
  CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞';

-- ---------- 成就 ----------
CREATE TABLE IF NOT EXISTS achievement_unlocks (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  user_id     BIGINT      NOT NULL,
  code        VARCHAR(32) NOT NULL,
  unlocked_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_au_user_code (user_id, code) COMMENT '防重复颁发',
  CONSTRAINT fk_au_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就解锁';
