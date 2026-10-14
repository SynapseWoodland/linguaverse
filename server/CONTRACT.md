# LinguaVerse 后端接口契约

- 服务：Spring Boot 3 / Java 17，默认端口 `8080`，统一前缀 `/api`
- 鉴权：`Authorization: Bearer <JWT>`（登录/注册/health 外全部需要）
- 字符：请求与响应均为 `application/json; charset=utf-8`
- CORS：仅允许 `http://localhost:5173`、`http://127.0.0.1:5173`

## 统一响应

成功：

```json
{ "code": 0, "message": "ok", "data": { } }
```

失败：`code` 与 HTTP 状态码保持一致。

```json
{ "code": 409, "message": "用户名已存在", "data": null }
```

## HTTP 状态与业务码

| HTTP / code | 含义 |
| --- | --- |
| 200 / 0 | 成功 |
| 400 | 参数校验失败 / 请求不合法 |
| 401 | 未登录或 token 失效 |
| 403 | 已登录但无权访问（如未解锁课时、越权删除） |
| 404 | 资源不存在 |
| 409 | 状态冲突（重复注册、重复结算等） |
| 429 | 触发限流（连续 5 次登录失败锁 10 分钟） |
| 500 | 服务端内部错误 |

## 安全红线

- 题目正确答案只存服务端（`grammar_questions.answer_index`、`listening_materials.answer_index`、`placement_questions.answer_index`），**任何响应都不得下发**。
- 课时按语种内 `lessons.seq_no` 全局顺序解锁；未解锁访问详情返回 **403**。
- 课时结算以 `(user_id, lesson_id)` 唯一键保证幂等：重复请求不得重复发放 XP/streak/成就。
- 密码仅以 BCrypt 存储，永不下发、不记日志。

## 端点清单（✅ 已实现 / 🚧 规划中）

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| GET | `/health` | 健康检查（含 DB SELECT 1） | ✅ |
| POST | `/auth/register` | 注册，返回 token + 用户信息 | ✅ |
| POST | `/auth/login` | 登录，返回 token + 用户信息；5 次失败锁 10 分钟 | ✅ |
| GET | `/me` | 当前用户资料 + enrollments | ✅ |
| PATCH | `/me` | 修改昵称/简介/头像种子/每日目标（仅 10/30/60/100 XP 四档，否则 400） | ✅ |
| POST | `/me/onboarding` | 三路径：`PLACEMENT` / `DIRECT` / `SKIP` | ✅ |
| GET | `/courses` | 语种 + L1–L6 级别树（released 标记） | ✅ |
| GET | `/courses/{lang}` | 语种课程树：单元/课时/完成状态/解锁 | ✅ |
| POST | `/enrollments` | 选学语种 `{langCode}`，幂等 | ✅ |
| GET | `/placement/{lang}` | 定级题（不含答案） | ✅ |
| POST | `/placement/{lang}` | 提交定级答案 `{answers:[]}`，0-3→L1 / 4-6→L2 / 7-10→L3 | ✅ |
| GET | `/lessons/{id}` | 课时详情四环节（不含任何答案）；未选课/未解锁 403 | ✅ |
| GET | `/review/due` | SRS 到期队列 `?lang=&limit=`（未掌握且 due_date≤今天） | ✅ |
| POST | `/review/rate` | 闪卡评分 `{langCode, word, rating:UNKNOWN/VAGUE/KNOWN}` | ✅ |
| PUT | `/lessons/{id}/progress` | 上报环节进度 `{stage:VOCAB/GRAMMAR/LISTENING/SPEAKING, score:0-100}`，upsert | ✅ |
| POST | `/lessons/{id}/grammar/submit` | 语法判题 `{answers:[{questionId,choiceIndex}]}`，返回逐题对错/正确答案/解析/总分 | ✅ |
| POST | `/lessons/{id}/listening/submit` | 听力判题 `{choiceIndex}`，返回对错/正确答案/解析 | ✅ |
| POST | `/lessons/{id}/speaking/submit` | 口语评分：`mode=RECOGNITION`（服务端按识别文本相似度判分，可回退 score）或 `SELF`（NEED_PRACTICE/OK/FLUENT=60/80/95） | ✅ |
| POST | `/lessons/{id}/complete` | 课时结算 `{durationSec}`，幂等；返 XP 明细(词×2+环节×4+准确率加成≤20)/accuracy/streak/nextLesson/新成就 | ✅ |
| GET | `/dashboard` | 六类聚合：XP/等级(每级500)、streak、今日XP/分钟/目标、近7天XP柱、各语种完成%、四模块均分、词汇分布 | ✅ |
| GET | `/today/recommendations` | 今日计划规则引擎：新用户定级/开课/设目标；老用户到期复习/续学/薄弱模块；≥3 条含 reason+deeplink，达标切鼓励文案 | ✅ |
| GET | `/posts` | 社区帖子分页 `?lang=en/ja/ko&page=&size=`（size≤50），时间倒序，含作者与当前用户 liked | ✅ |
| POST | `/posts` | 发帖 `{langCode?, title?, content}`，content 必填；首帖解锁 social | ✅ |
| POST | `/posts/{id}/like` | 点赞 toggle，返 liked/likeCount，唯一键并发幂等 | ✅ |
| GET | `/posts/{id}/comments` | 评论列表（时间正序，含作者） | ✅ |
| POST | `/posts/{id}/comments` | 发表评论 `{content}`，comment_count 自增 | ✅ |
| DELETE | `/posts/{id}` | 删帖（仅作者，越权 403，不存在 404；逻辑删除+评论物理级联） | ✅ |
| GET | `/achievements` | 8 枚成就目录 + 解锁状态/时间 + current/target 实时进度 | ✅ |

## 注册 / 登录响应体

```json
{
  "token": "<jwt>",
  "user": {
    "id": 1,
    "username": "emma",
    "nickname": "Emma",
    "avatarSeed": "E",
    "dailyGoalXp": 30,
    "streak": 0
  }
}
```

## 本地运行

```powershell
# 1. MySQL 8（容器）
docker run -d --name linguaverse-mysql `
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=linguaverse `
  -p 3306:3306 mysql:8

# 2. 构建（需要 JDK 17+）
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-27'
mvn -DskipTests package

# 3. 启动
java -jar target/linguaverse-server-1.0.0.jar
```

可覆盖环境变量：`DB_HOST` `DB_PORT` `DB_NAME` `DB_USER` `DB_PASSWORD` `JWT_SECRET`。
启动自动执行 `classpath:db/schema.sql`（`CREATE TABLE IF NOT EXISTS`）并幂等灌种课程语料；演示账号 `demo / demo1234`。

## 种子数据规模

3 语种 × L1–L3 × 2 单元 × 2 课时 = **36 课时**；每课时 8 词 + 4 道语法题 + 1 段听力（含 1 题）+ 5 跟读句；三语各 10 道定级题；L4–L6 为 released=0 的 upcoming 占位；另有 5 篇社区种子帖。
