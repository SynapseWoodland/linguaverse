# LinguaVerse 多语种在线学习平台 - 实施计划（MVP）

> 三阶段推进：**Phase 0 全量前端原型**（按 design-plan.md「语言护照」设计系统，mock 数据，覆盖 MVP/P1/P2 共 53 个视图）→ **Phase 1 Spring Boot 后端 + 真实对接（MVP 上线范围）** → **Phase 2 打磨与回归**。双工程：`client/`（Vite + React 18 + TS + Tailwind v4）、`server/`（Java 17 + Spring Boot 3 + MyBatis-Plus + MySQL 8）。

---

# Phase 0：全量前端原型（mock 驱动）

> 全部任务在 `client/` 完成；视觉与交互严格遵循 design-plan.md；数据来自 `src/mocks/`，API 层以统一接口封装，Phase 1 仅替换实现。每个视图显示阶段角标（M/1/2）。

> **执行记录（2026-10-13）**：P0-1~P0-10 已完成，交付形态为**纯静态 HTML 原型**（无构建、双击即开），位于 `prototype/`：`index.html` 索引页直达全部 **53 个视图**（`prototype/pages/`），设计系统在 `assets/passport.css`、公共交互在 `assets/app.js`（注入学员/后台两套外壳、阶段角标、翻卡、toast、modal 等）。该原型为"不废弃式原型"，视觉令牌、页面结构与真实语料将在 Phase 1 迁移为 React client。走查结论：53 页 HTTP 200、内部链接与资产无 404、翻卡等交互正常、console 0 错误（仅 favicon 404）、1440/390 宽度均无横向溢出。下列各 P0 任务 Status 视为 `done`。

## Task P0-1: 设计系统工程化、应用外壳与 mock 基座

- **Status**: `done`
- **Priority**: high
- **Depends On**: 设计计划获批
- **Description**:
  - Vite React TS 严格工程（Tailwind v4、Router、Zustand、lucide-react）；引入字体（Fraunces、Noto Serif/Sans SC/JP/KR）。
  - 设计令牌落地：Paper/Ink/Passport/Stamp/Visa Gold/Thread 与三语种色；字体阶梯；纸线、纸叠投影；工具类与语义组件。
  - 基础组件：邮票齿孔卡（PerforatedCard）、签证贴纸（VisaSticker）、圆邮戳（RubberStamp，含盖章动画）、目标环、路线站点、纸质感按钮 primary/ghost/danger、输入/校验态、Toast、Modal、分段进度、空状态/加载骨架/错误重试。
  - 两套外壳：学员端（顶部导航+移动底部导航）、管理后台端（侧边栏）；原型角标组件（实心/描边/虚线邮戳 M/1/2）。
  - 路由总表（53 视图全部注册，含原型展示用导航索引页 `/archive` 列出全部页面直达）；mock 数据层（课程树、用户、活动、帖子、成就、排行榜、会员套餐等真实感样例数据）。
- **Acceptance Criteria Addressed**: AC-16、AC-U1（基础）
- **Test Requirements**:
  - `rule` TR-P0-1.1: `npm run dev` 可访问，`/archive` 索引页可直达全部已建视图（随批次增长最终 53 个可达），证据为走查。
  - `rule` TR-P0-1.2: `npm run build` 零错误；390/1440 下外壳无横向溢出，证据为构建输出与截图。
  - `rubric` TR-P0-1.3: 组件呈现"语言护照"材质（纸/邮戳/齿孔/签证），与 SaaS 卡片模板有可辨识差异；scale 1-5；anchors 1=通用模板，3=整洁普通，5=材质独特统一；threshold >= 4；证据为组件廊截图。

## Task P0-2: 游客与认证视图（P01–P05、P47、P48）

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task P0-1
- **Description**: 落地页（护照封面 hero、三语大号外文词、三邮戳、卖点、CTA，动效仅首屏一次编排）；注册页、登录页（错误态/限流锁定提示态）；P1 找回密码页（邮箱验证三步）；Onboarding（欢迎→语种卡片→定级/直学/稍后）；404、断网异常页（重试按钮态）。
- **Acceptance Criteria Addressed**: FR-1/17、FR-P1-01、NFR-11、AC-U1
- **Test Requirements**:
  - `rule` TR-P0-2.1: 各视图均可从导航/流程到达，表单有校验/错误/成功三态，按钮跳转符合流程，证据为走查截图。
  - `rubric` TR-P0-2.2: 落地页 hero 记忆点维度；scale 1-5；anchors 1=表单页感，3=信息完整，5=护照封面有启程冲动且无 AI 默认套路；threshold >= 4；证据为截图。

## Task P0-3: 学习主区、课程中心与定级（P06、P09–P12、P07、P08）

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task P0-2
- **Description**: 学习首页（问候、等级/Streak、今日目标环、今日计划卡含理由文案与两种账户 mock：新用户/老用户、快捷入口、达标鼓励态）；课程中心（三语种卡含进度、P2"新语种预告"卡）；课程地图（虚线旅程、站点三态、L4 可学占位 [1]、L5/L6 锁定 [2]、签证贴纸级别）；定级答题页与结果页（全对/全错两种结果态）；P1 消息中心；P1/P2 提醒设置（多渠道开关）。
- **Acceptance Criteria Addressed**: FR-3~6/14/18、FR-P1-04、FR-P2-01/02/09、AC-3/4/5/12/18、AC-U3
- **Test Requirements**:
  - `rule` TR-P0-3.1: 地图三态/新语种预告/L4 占位可辨；首页提供新老两种 mock 切换可见推荐差异；目标环三档与达标态齐全，证据为走查。
  - `rubric` TR-P0-3.2: 路线图独特性与信息清晰度；scale 1-5；anchors 1=通用列表，3=清晰但普通，5=旅程地图直观且有触感；threshold >= 4；证据为截图。

## Task P0-4: 课时学习四环节、结算与复习（P13–P19）

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task P0-3
- **Description**: 课时护照对页（左学习右批注、四环节进度点、计时态）；单词闪卡（翻面、发音按钮态、认识/模糊/不认识、张数）；语法（讲解卡、单选/填空/改错、即时对错解析、末屏正确率）；听力（播放控制条、逐句高亮、语速、原文显隐；P2"真人发音"角标变体）；口语（识别评分态 + 三档自评降级态、麦克风权限拒绝态）；结算盖章仪式页（XP/词数/正确率/Streak、徽章弹层、下一站）；复习中心（到期队列、空状态）。全部交互以 mock state 驱动。
- **Acceptance Criteria Addressed**: FR-7~11、FR-P2-03、AC-6~10、AC-U1
- **Test Requirements**:
  - `rule` TR-P0-4.1: 四环节流程可走通到结算；每种题型至少两种结果态；盖章动画在 reduced-motion 下降级，证据为走查/截图。
  - `rubric` TR-P0-4.2: 学习页专注度与结算仪式感；scale 1-5；anchors 1=普通后台练习，3=可用，5=对页沉浸+盖章时刻有成就感；threshold >= 4；证据为截图。

## Task P0-5: 词汇工具、搜索收藏与 AI/P2 实验视图（P20–P25）

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task P0-4
- **Description**: P1 生词本（筛选/移除/导出按钮态）；P1 遗忘曲线页（retention 图表）；P1 搜索页（三域结果/空结果态）；P1 收藏页；P2 AI 对话练习页（对话气泡、话题卡、反馈卡）；P2 口语 PK 页（匹配中/对战/比分三态）。P2 视图统一虚线戳并显示"规划中"角标，功能可点但注明非本期。
- **Acceptance Criteria Addressed**: FR-P1-02/03/10、FR-P2-04
- **Test Requirements**:
  - `rule` TR-P0-5.1: 六视图可直达，关键状态（空/加载/结果/规划中）齐全，证据为走查。
  - `rubric` TR-P0-5.2: P2 概念页在保持护照语言一致的前提下呈现未来感；scale 1-5；threshold >= 4；证据为截图。

## Task P0-6: 进度、成就、榜单与证书视图（P26–P31）

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task P0-4
- **Description**: 仪表盘（等级环、目标环、Streak、今日/累计分钟、7 天柱、语种进度、四模块、词汇环，自绘 SVG）；成就墙（邮戳徽章网格、已得日期、未得条件进度、P1 称号区）；P1 徽章分享卡（明信片）；P1 排行榜（周榜 Tab、自己排名条）；P2 学习证书；P2 数据导出页。
- **Acceptance Criteria Addressed**: FR-13/16、FR-P1-06/07、FR-P2-06/10、AC-11/14
- **Test Requirements**:
  - `rule` TR-P0-6.1: 至少 8 枚徽章的已得/未得两种视觉齐全；图表在窄屏可读，证据为截图。
  - `rubric` TR-P0-6.2: 徽章邮戳与证书的收藏质感；scale 1-5；threshold >= 4；证据为截图。

## Task P0-7: 社区与社交全量视图（P32–P39）

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task P0-2
- **Description**: 社区信息流（语种筛选、邮票卡、点赞态、自己的帖删除确认、内联评论区 MVP 态）；发帖编辑器（字数、标签、发布成功态）；P1 帖子详情（盖楼回复）；用户主页；关注/粉丝列表；私信列表与对话窗；举报弹层。
- **Acceptance Criteria Addressed**: FR-15、FR-P1-05、AC-13
- **Test Requirements**:
  - `rule` TR-P0-7.1: 发帖→置顶（mock）、点赞/评论/删除/举报状态流可演示，证据为走查。
  - `rubric` TR-P0-7.2: 邮票卡信息流与明信片详情的阅读氛围；scale 1-5；threshold >= 4；证据为截图。

## Task P0-8: 我的、会员、支付、班级与下载视图（P40–P46）

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task P0-6
- **Description**: 个人资料（含统计、主修语）；P1 账户与安全（头像上传态、改密、邮箱、注销/重置二次确认）；每日目标三卡设置；P2 会员订阅套餐页与收银台弹层（支付结果成功/失败态）；P2 班级教务（建班、作业、学情表）；P2 下载 App/PWA 引导。
- **Acceptance Criteria Addressed**: FR-2/18、FR-P1-01、FR-P2-05/07/08
- **Test Requirements**:
  - `rule` TR-P0-8.1: 危险操作（注销/重置/支付）均有二次确认与结果态，证据为走查。
  - `rubric` TR-P0-8.2: 会员页 Visa Gold 质感克制不浮夸；scale 1-5；threshold >= 4；证据为截图。

## Task P0-9: 管理后台视图（P50–P53）

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task P0-3
- **Description**: 独立后台外壳与登录页；数据看板（用户/活跃/内容指标卡与图表、监控指标占位区）；用户管理（表格、封禁确认）；课程内容管理（课程树+题目编辑器形态）；内容审核（举报队列、删除操作态）。视觉沿用 Paper/Ink 但布局为高密度管理台，不用消费端邮戳装饰。
- **Acceptance Criteria Addressed**: FR-P1-09、FR-P1-14、FR-P2-13
- **Test Requirements**:
  - `rule` TR-P0-9.1: 四页面可达，表格在 1440 下密度合理、390 不破版，证据为截图。
  - `rubric` TR-P0-9.2: 后台专业度（与消费端气质区隔）；scale 1-5；threshold >= 4；证据为截图。

## Task P0-10: 全量原型走查与设计自审

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task P0-5、P0-6、P0-7、P0-8、P0-9
- **Description**: 53 视图需求覆盖矩阵逐条核对（角标/真实内容/状态完整性）；390/768/1440 三宽度抽查；键盘与焦点抽查；按 skill"去掉一件配饰"原则做视觉减法；产出原型走查截图集；启动本地预览供评审。
- **Acceptance Criteria Addressed**: 全部 FR 的视图覆盖、AC-16
- **Test Requirements**:
  - `rule` TR-P0-10.1: 覆盖矩阵中每个 FR 至少一个可达视图且无 lorem 空占位；/archive 计数为 53，证据为矩阵表与走查记录。
  - `rule` TR-P0-10.2: 三宽度抽查无横向滚动/遮挡；控制台无 error，证据为截图与控制台。
  - `rubric` TR-P0-10.3: 全站沉浸品质终检（对齐 AC-U1）；scale 1-5；anchors 1=模板感，3=整洁，5=语言护照世界观统一、盖章时刻记忆鲜明；threshold >= 4；证据为关键页截图集。

---

# Phase 1：Spring Boot 后端与 MVP 真实对接

> Phase 0 原型已完成全部页面；契约以 `server/CONTRACT.md` 为准。
>
> **前置说明**：Phase 0 实际交付为 `prototype/` 纯静态 HTML，`client/` React 工程尚未搭建，故新增 **Task 2.5**（React 工程迁移与视图组件化）作为 Task 3–10 全部前端接真工作的共同前置；Task 2.5 完成后，各 Task 只做 client 数据层切换（mock → 真实 API），页面组件复用。

> **执行记录（2026-10-14）**：Task 1–3 已完成并在 MySQL 8 容器（`linguaverse-mysql`，root/root，3306）上联调通过。
>
> - **Task 1**：`server/` 工程（Spring Boot 3.5.0 / Java 17 字节码 / MyBatis-Plus 3.5.9 / jjwt 0.12.6），21 张表幂等 schema、JWT 无状态安全链（白名单 `/health`、`/auth/register`、`/auth/login` + OPTIONS）、BCrypt、统一响应/全局异常/登录内存限流（5 次锁 10 分钟）、MySQL 分页、CORS 仅放行 5173；[CONTRACT.md](computer://d:/VideCoding/temp_20261013_01/server/CONTRACT.md) 建档。实测：`GET /api/health` 200、无/坏 token 401、CORS 预检 200、`mvn clean package` 零错误。注：本机宿主 8080 被其它项目的 `attu` 容器占用，联调临时用 `--server.port=18080`，交付默认仍为 8080。
> - **Task 2**：语料以 Java 结构库维护（[EnContent.java](file:///d:/VideCoding/temp_20261013_01/server/src/main/java/cn/linguaverse/seed/EnContent.java)、[JaContent.java](file:///d:/VideCoding/temp_20261013_01/server/src/main/java/cn/linguaverse/seed/JaContent.java)、[KoContent.java](file:///d:/VideCoding/temp_20261013_01/server/src/main/java/cn/linguaverse/seed/KoContent.java)），由 [ContentSeeder.java](file:///d:/VideCoding/temp_20261013_01/server/src/main/java/cn/linguaverse/seed/ContentSeeder.java) 在启动事务内自检后灌种。落库计数：languages=3、levels=18（L4–L6 released=0 占位）、units=18、lessons=36、vocab=288、grammarPoints=36、grammarQuestions=144、listening=36、speaking=180、placement=30、posts=5、comments=5；三语 CJK 抽查无乱码；二次启动日志 `课程内容已存在，跳过灌种` 且行数不变；演示账号 `demo/demo1234`。
> - **Task 3**：`POST /auth/register`、`POST /auth/login`、`GET/PATCH /me`、`POST /me/onboarding`（PLACEMENT/DIRECT/SKIP 三路径）已实现。实测：注册 200、重复注册 409、非法用户名 400、错密 5 次返回 401×5 后第 6 次 429、正确密码 200、坏 token 401、`/me` 与 PATCH（dailyGoalXp/bio/中文）200、DIRECT 落 enrollment(placementLevel=1)、SKIP 不落库、非法语种 400；DB 密码全部为 `$2a$10$` BCrypt 无明文。
> - **Task 4**（2026-10-14）：`GET /courses`、`GET /courses/{lang}`、`POST /enrollments`（幂等）、`GET·POST /placement/{lang}`、`GET /lessons/{id}` 已实现，新增 `ProgressService` 按语种内 `seq_no` 全局顺序解锁。实测：课程树 L1–L3 released=true、L4–L6=false 且无 units（upcoming 无内容出口）；未选学访问课时 403、选学后第一课 200（8 词/4 语法题/5 跟读句）、第二课 403、不存在课时/语种 404；定级全错→L1、4 对→L2、全对→L3，结果与 PLACEMENT 流水落库、enrollment 回填定级级别；试卷与课时详情 JSON 不含 `answerIndex`/题目解析；选项越界 400、畸形 JSON 400（补了 `HttpMessageNotReadableException` 全局处理）。
> - **Task 5**（2026-10-14）：`GET /review/due`、`POST /review/rate`、`PUT /lessons/{id}/progress` 已实现（SrsService / StageProgressService）。SRS 规则实测：UNKNOWN→stage0 当天再到期；VAGUE→stage+1 次日；KNOWN→stage+2 三天后，stage≥6 mastered 并退出队列；到期队列按 due_date/stage 排序、mastered 排除、limit 上限 100；首次评分必须是该语种已解锁课时词表中的词（假词 404、第 2 课锁定词 403、未选学 403、非法枚举 400），唯一键 `uk_wm_user_lang_word` 保证一人一词一行。环节进度按 `(user, lesson, stage)` upsert，重复上报不新增行，课时详情 `completedStages` 可恢复；锁定课时 403、非法 stage/score(101) 400、课时不存在 404。
> - **Task 6**（2026-10-14）：三个判题端点已实现（QuizService/QuizController），判题后响应才含正确答案与解析。语法：全对 100/半对 50，逐题返回 correct/correctIndex/explanation，题数不符/混入他课题目/越界下标均 400。听力：对=100 错=0 并回正确答案与解析，下标越界 400。口语双路径均走通：RECOGNITION 服务端对识别文本做归一化字符 Levenshtein 相似度判分（实测近似句 95-100、乱读句 9、总分 81，逐句三档反馈），缺 transcript 时回退客户端 score 0-100；SELF 三档自评 NEED_PRACTICE/OK/FLUENT=60/80/95（实测总分 82），缺档位/非法 mode/句数不符 400。每次提交写 activities 流水（type=GRAMMAR/LISTENING/SPEAKING、xp=0 练习不发 XP、payload_json 记明细、可重试保留历史），并 upsert 对应 lesson_stage_progress（最新分 50/0/82）；未选学 403、锁定课 403、课时不存在 404；GET 课时详情复查仍无 answerIndex/题目解析（grammar.explanation 为知识点讲解，非答案）。
> - **Task 7**（2026-10-14）：`POST /lessons/{id}/complete` 已实现（CompletionService 单事务）。前置：四环节成绩齐全（缺环节 400 并列明缺失项）、未选学 403、未解锁 403、课时不存在 404、durationSec 负数/超 14400 为 400。XP 明细：base=词数×2+四环节×5（8 词课时=36），bonus=round(accuracy%×20)，满分课 56 XP；accuracy=四环节均值（92.5→93）。同事务写 lesson_completions（uk_lc_user_lesson 幂等）+ activities(LESSON_DONE 账本，payload 存明细) + users.streak/last_study_date（昨天→+1、同日不增、断签重置 1）+ 成就。幂等实测：重复结算 alreadyCompleted=true、durationSec 不被覆盖、XP 明细从流水还原、streak/成就不重发，并发由唯一键 + DuplicateKeyException 兜底。成就（AchievementService 结算路径 5 枚，对齐既有 AchievementCatalog）：first_step、streak_3、streak_7、lessons_10、perfect 均实测解锁且不重复；words_50/polyglot/social 留 Task 9/10。解锁链实测：连完 seq1–10 后课程树 1–10 completed、11 unlocked、12 locked；XP 账本 SUM=542（56+9×54）与结算一致。
>
> - **Task 2.5（阶段进展，2026-10-14）**：`client/` 工程骨架已搭好（Vite 6 / React 18 / TS 严格 / Tailwind v4 / react-router-dom 6 / zustand 5 / lucide-react）。设计令牌迁移完成：`@theme` 定义全部色板/字体/圆角/阴影并生成同名工具类，`src/styles/passport.css` 原样迁移原型全部语义类（裸 CSS 不参与 tree-shake，`:root` 别名桥接 `--paper` 等旧变量名）；两套外壳组件化（`LearnerShell` 顶栏+移动 tabbar、`AdminShell` 侧边栏+`admin-body` 背景切换），阶段角标 `PhaseBadge`/`MiniPhase`；53 视图路由全部由 `src/lib/routes.ts` 单一清单驱动（含每页 pid/path/phase/shell/nav），`/archive` 索引计数 53 与原型一致；统一 API 基座 `src/lib/api.ts`（JWT 注入、`R<T>` 解包、401 清会话回跳登录页）与会话 store `src/lib/session.ts` 已预留，`VITE_API_BASE` 默认 8080、本机联调 `.env.development` 指向 18080。实测：`npm run build`（tsc+vite）零错误；`/archive`、学员外壳（1440/390）、后台外壳截图走查正常，console 0 error（仅 favicon 404 + RR v7 future flag 提示）。**剩余**：53 视图本体迁移（当前为统一占位页 `PlaceholderPage`）、公共交互（翻卡/toast/modal/计时）React 化、逐页对比走查。
>
> - **Task 2.5（第二阶段，2026-10-14）**：A 组 7 个无外壳视图本体迁移完成（P01 landing / P02 register / P03 login / P04 forgot-password / P05 onboarding / P47 404 / P48 error），视觉沿用 passport.css 语义类，与原型逐页一致；landing 按区块拆为文件内小组件（TopBar/Hero/LangStamps/Modules/PlanPreview/Community/FinalCta），注册/登录保留原型演示错误态与默认值，表单提交以 navigate 模拟原型跳转（register→onboarding、login→home）。公共交互组件 React 化：`lib/toast.ts`（zustand 单例 + 1800ms 自动隐藏）+ `components/Toaster.tsx`（挂于 RouterProvider 旁全站可用，error 页「重新连接」实测 300ms 内 display:block、约 2s 后自动隐藏）、`Modal`（受控 open、点遮罩关闭）、`FlipCard`（.flip 翻面、支持受控）、`Tabs`（.tabs 分段页签 + 面板 .hide 切换）、`PlayButton`（[data-play] 播放态 2200ms 复原）、`lib/useCardNav.ts`（闪卡 prev/next + n/total 计数）；新增 `lib/css.ts` CSSVars 类型承载原型内联 CSS 变量（--s/--p/--size）。router.tsx 改为 REAL 映射表按 path 替换占位页，其余 46 页仍走 PlaceholderPage。**有意偏差**：onboarding 原型 data-tab 索引错位（点「开始选语种」实跳第三步）且步骤指示静态不更新，React 版修为真三步向导（欢迎→选语种→定级/直学，指示器随步推进、语种卡可点选、默认 en），首屏因此只渲染欢迎卡（原型首屏为欢迎+选语种同屏），属缺陷修复而非走样。实测：`npm run build`（tsc 严格 + vite）零错误；7 页 1440 截图走查渲染正常，register 盖章动画 / landing hero / 护照插画与原型一致；toast 经 DOM 断言验证；console 0 error（仅 RR v7 future flag 提示）。环境限制：本机 playwright 窗口最小宽 562px，390px 视口未生效，窄屏走查留待设备仿真补测。**剩余**：B–G 组 46 页本体迁移与 mock 数据层。
> - **Task 8**（2026-10-14）：`GET /dashboard`、`GET /today/recommendations` 已实现（stats 包 DashboardService/RecommendationService）。仪表盘六类指标实测：XP/等级（每 500 XP 一级，t7b 542 XP→Lv2 余 42）、streak=7、今日 XP/分钟/目标（10 课×120s=20 分钟，DB SUM 对账 542/20/10 一致）、近 7 天 XP 柱、各语种完成%（en 10/12=83% 均分 91）、四模块均分（t6a GRAMMAR 75/2 次、LISTENING 50/2、SPEAKING 86/3、词汇取掌握率）、词汇分布（学习中/掌握/到期 byLang）。学习前后变化实测：完成 300s 课时后今日 0 分钟/0 XP → 5 分钟/52 XP。目标档位：PATCH /me dailyGoalXp 收紧为 10/30/60/100 四档（5/50 均 400，60 持久化，仪表盘分母即时变化），默认 30。推荐引擎两态对比：新用户（未选学）固定 TAKE_PLACEMENT/DIRECT_START/SET_GOAL 三条；老用户按规则输出 REVIEW_DUE（UNKNOWN 标记后当天到期，含数量与 /review?lang= deeplink）、FIRST_LESSON→/lessons/1（零完成）、CONTINUE_LESSON（完成 L1 后计划刷新为 /lessons/2，t7b 续学指向 /lessons/11）、WEAK_LISTENING（近 30 次均分<70，附课时名与理由）、KEEP_STREAK（达标且 streak≥2），兜底 EXPLORE_LANG/ACHIEVEMENT_WALL；达标后 message 切鼓励文案，未达标显示差额；无 token 401。
>
> - **Task 9**（2026-10-14）：社区六端点已实现（community 包 CommunityService/CommunityController）。`GET /posts` 时间倒序分页（MP 分页插件）：种子 5 帖 total=5/pages=3（size=2），`lang=en` 过滤 2 条、ja 1 条；hidden/deleted 帖自动过滤；VO 含作者(id/username/nickname/avatarSeed)与当前用户 liked。发帖：无 langCode/title 允许（闲聊贴 lang=null），content 必填且≤2000，非法 lang=fr/空白内容 400，新帖置顶。点赞 toggle：A 赞→liked=true/count=1（feed 同步），再点取消，A+B 同赞 count=2、B 取消后 1；uk_like_post_user + DuplicateKeyException 兜底并发。评论：种子帖列表（post1=2 条正序），发评论后 comment_count 2→3，空内容 400。删除：B 删 A 的帖 403，A 删自己 200；删后 feed 消失（deleted=1 行保留）、评论列表/点赞/再删均 404，评论物理级联（post6 comments=0；post1=3、post2/3=1、post5=1 对账一致）。无 token 401；`@RequestParam` 校验（lang 非法/size>50/page<1）由 ConstraintViolationException 统一映射 400（GlobalExceptionHandler 新增处理）。social 成就：t9a 首帖解锁，第二帖不重复（DB=1）。
>
> - **Task 8.5**（2026-10-14）：后端单测基座落地（JUnit 5 + Mockito + AssertJ，零新依赖、Mapper 全 mock 不连库），6 个测试类 **70 用例全绿**。覆盖：PlacementServiceTest 12（0/3/4/6/7/10 正确三档边界、题数/越界/缺项 400、语种与无题 404、试卷 JSON 不含 answerIndex）、QuizServiceTest 24（语法 100/50 与乱序提交按题号排序回执、混入他课/重复/越界/题数 400、环节进度 upsert update 分支；听力 100/0 与无材料 400；口语 RECOGNITION 相似度 100/91/0→均值 64、缺 transcript 回退客户端分、SELF 60/80/95→78、缺档位/句数/混入 400；similarityScore 归一化/CJK/全异边界；课时 404、未选学/锁定 403）、CompletionServiceTest 13（accuracy 92.5→93、XP 36+19=55、满分 56、Streak 同日不变/昨日+1/断签重置/首次为 1、缺环节 400 列明缺失、幂等回放从流水还原 XP 且不重发、DuplicateKey 并发兜底、404/403）、SrsServiceTest 11（UNKNOWN 归零当天/VAGUE+1 次日/KNOWN+2 三天、stage6 mastered、VAGUE 到 6 不触发 mastered、403/404、due 映射与 mastered=null 兜底）、JwtServiceTest 5（往返/篡改/异密钥/过期/畸形）、LoginRateLimiterTest 5（第 5 次锁 429、成功清零、用户名归一化、IP 隔离、窗口过期放行）。**变异抽查（TR-8.5.2）三处均变红**：定级阈值 0.4→0.5（fourCorrectSuggestL2）、KNOWN 间隔 +3→+2 天（knownAdvancesTwo 等 2 例）、WORD_XP 2→3（completeHappyPath 等 2 例），验证后已全部还原并复跑全绿。注意：本机 mvn 默认 JDK 8（JAVA_HOME 指向 jdk-8.0.482），跑构建/测试需 `$env:JAVA_HOME='C:\Program Files\Java\jdk-27'`。TR-8.5.1/8.5.2 满足，Status 视为 `done`。Task 9 社区随行单测（TR-9.2，同日补齐）：CommunityServiceTest 12 用例全绿——发帖字段规整与首帖成就、空白标题/语种归 null、点赞 +1/取消 -1 下限 0/DuplicateKey 幂等、隐藏帖与不存在帖 404、评论计数 +1 与作者回执、作者删帖物理级联评论、越权删帖 403 且不执行删除、信息流 liked 标记与作者映射；全套累计 82 用例。TR-9.2 满足。
>
> - **Task 10**（2026-10-14）：成就系统收尾与 MVP 全量真实对接。
>   - **T10a 后端**：新增 `GET /achievements`（AchievementController/AchievementQueryService + `dto/AchievementVO` 独立文件 record：code/nameZh/descZh/icon/unlocked/unlockedAt/current/target），8 枚徽章实时进度：first_step→完成课数、streak_3/7→users.streak、lessons_10→10、words_50→word_mastery mastered=true 计数 /50、perfect→accuracy=100 的 completion 数、polyglot→enrollment 数（阈值 **3 语种**）、social→posts 数。`AchievementService`（位于 `learn.service`）补 `evaluateOnWordMastered(userId)`（SrsService.rate 仅在 becameMastered 时调用）与 `evaluateOnEnrollment(userId)`（EnrollmentService upsert 新 insert 后调用），统一走幂等 `unlockAll`。实测（t10a）：新用户 8 枚全锁进度 0；en+ja+ko 后 polyglot 解锁、重复 enroll 不重发（DB=1）；49 条 mastered 不发、第 50 条经 `POST /review/rate`（body `{langCode,word,rating}`）评 KNOWN→stage6 后 words_50 50/50 解锁；无 token 401。
>   - **T10b 前端接真**：新建 [sdk.ts](file:///d:/VideCoding/temp_20261013_01/client/src/lib/sdk.ts)（全部端点 TS 封装；followDeeplink、signOut、stageProgress 用 PUT）与 [ui.tsx](file:///d:/VideCoding/temp_20261013_01/client/src/lib/ui.tsx)（useApi 三态/Loading/ErrorState/RequireAuth）。18 个 MVP 页全部切真：登录注册、Onboarding（PLACEMENT/DIRECT/SKIP）、定级答题+结果、课程中心/地图、课时四环节枢纽（闪卡调 rate、语法、听力 speechSynthesis、口语 webkitSpeechRecognition→SELF 三档降级、四环节齐才显盖章）、结算页（XP 明细/新成就/alreadyCompleted）、复习（?lang= 到期队列）、Home（目标环 SVG+streak+推荐卡）、Dashboard、Achievements 进度墙、社区（列表过滤分页/发帖/详情评论盖楼/点赞/作者删除）、Profile（资料+目标四档 10/30/60/100+退出）；RequireAuth 守卫白名单外全部路由。
>   - **T10c 浏览器端到端走查（TR-10.1）**：新注册 e2e1 单会话跑通「onboarding→课程地图→L1 闪卡 8/8→语法 4 题提交（对错/正确答案/解析/SCORE 齐全）→听力判题→口语 SELF 95→盖章结算（STAMPED、XP 46=base36+bonus10、accuracy 49 与四环节对账一致、streak 1、first_step 弹层）→Home（46/60 XP 环、续学指向 L2《数字与时间》、弱项语法练习卡）→Dashboard（Lv1、今日 46/30 已达标、en 1/12、7 天柱、四模块）→成就墙 1/8→社区发帖（首帖后 social 实时解锁，墙变 2/8）→评论→点赞 ♥1→列表作者删除按钮可见」，DB 对账 first_step/social 各 1 行不重发；全程 console 0 红错。走查中发现并修复 1 个真实 bug：`PATCH /me` 返回 UserVO 而非 MeResponse，ProfilePage 保存后整体替换 me 导致渲染崩溃（avatarSeed undefined，React Router ErrorBoundary），修为合并 `me.user` 且 session 同步用返回 UserVO，复测每日目标切 60 XP 正常、0 红错。
>   - **TR-10.2 单测**：新增 [AchievementServiceTest.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/learn/service/AchievementServiceTest.java) 13 用例 + [AchievementQueryServiceTest.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/achieve/service/AchievementQueryServiceTest.java) 4 用例，纯 Mockito、Mapper 全 mock 不连库。评估器边界：结算候选集（课数 1/3/9/10、streak 1/3/7、accuracy 99/100 组合）、首帖 social、mastered 49/50、语种 2/3 的触发与不触发；**不重发**用同一动作二次评估验证（首轮 5 枚全插、次轮 5 次查询均命中已有 → 返回空且 insert 计数仍为 5），另有"已拥有→返回 null 且 never insert"用例。成就墙：8 枚目录顺序与 target(1/3/7/10/50/1/3/1)、进度映射（曝光课数 5→first_step 收敛 1、mastered 60→min 50、streak 3→streak_7 收敛 3）、user/streak 缺失按 0、重复 code 去重取首条。实测：`mvn test` **99 用例全绿 BUILD SUCCESS**（82 + 13 + 4）。TR-10.1/10.2 满足。
>
> - **Task 11**（2026-10-14）：沉浸式打磨、响应式与无障碍（纯前端，server 无改动）。
>   - **修复与增强**：① 断网——[api.ts](file:///d:/VideCoding/temp_20261013_01/client/src/lib/api.ts) fetch 包 try/catch，断网抛 `ApiError(0,'网络连接已断开，请检查网络后重试')`、非 JSON 响应（502 等）给「服务器响应异常」；新增全局 [OfflineBanner.tsx](file:///d:/VideCoding/temp_20261013_01/client/src/components/OfflineBanner.tsx)（online/offline 事件，固定红条 role=status aria-live=assertive，恢复自动消失）挂于 main.tsx。② 无障碍——Loading 加 role=status/aria-live、ErrorState role=alert；主导航两套 nav 加 aria-label、活动项 aria-current=page、图标 aria-hidden、头像入口 aria-label；点赞按钮 aria-pressed+语义 aria-label（社区列表/详情）；语法/听力选项按钮 aria-pressed（选中态不再只靠颜色）；登录/注册/资料/发帖全部 label htmlFor↔id 关联、评论框 aria-label、目标档与语种按钮组 role=group+aria-pressed。③ 焦点——补全全局 `a/button/[tabindex]:focus-visible` 邮戳红焦点环（offset 2px）。④ 动效减法——reduced-motion 从仅 .stamp-anim 扩为全局 animation/transition≈0（实测 .stamp-anim 计算后 1e-05s）；补上 Loading 实际引用但缺失的 `@keyframes lv-spin`（此前转圈静默失效）。⑤ 对比度——`--color-muted` #6e7785→#5f6875（纸色底 3.88:1→4.8:1，过 WCAG AA 小字），错误文案与 .text-stamp 统一走 --stamp-deep #a53824（5.6:1，原 #c2452f 仅 4.29:1），视觉几乎无感。
>   - **TR-11.1 证据（Playwright/CDP，390 物理窗口受限故用 Emulation.setDeviceMetricsOverride 精确锁 CSS 宽）**：390/768/1440 × 13 个 MVP 页（home、courses、course-map、lesson、grammar、listening、speaking、review、dashboard、achievements、community、post-editor、profile）逐页 `scrollWidth==clientWidth`、越界元素 0（成就页 GOT 邮戳仅旋转包围盒外扩，不产生横向滚动）；截图存 `_walk/t11-{390,768,1440}-*.png`。**纯键盘链路**：Tab 定位 #login-username/#login-password→键入→Enter 登录跳 /home；L2 语法页 Tab+Enter 逐题选 4 个 A（aria-pressed）→Tab 到提交答案→Enter，SCORE 与 4 处正确答案标记渲染；社区 Tab 到点赞按钮→Enter，♥1→♡0、aria-pressed true→false。**断网**：SPA 内 context.setOffline(true) 后跳仪表盘，顶部红条 + ErrorState「✗ 网络连接已断开，请检查网络后重试」；恢复联网后点重试，横幅消失、Lv1 数据正常。除断网本身产生的 2 条浏览器网络错误外 console 0 error。
>   - **TR-11.2**：三宽度截图终检，护照质感（纸底/邮戳/齿孔卡/目标环/三语排版）在 390 单列与 1440 四列下均统一，自评 4/5。`npm run build` tsc+vite 零错误（311.02 kB JS / 25.71 kB CSS）。
>
> - **Task 12**（2026-10-14）：双构建 + 全量 E2E 回归（新干净账号 t12a/user_id=27、第二账户 t12b/user_id=29；后端 18080、前端 5173）。
>   - **TR-12.1 双构建**：后端 `mvn clean package`（JDK 27）BUILD SUCCESS，**99 用例全绿**，产物 `linguaverse-server-1.0.0.jar` 可启动、`GET /api/health` UP；前端 `npm run build` tsc+vite 零错误（311.02 kB JS / gzip 95.87、25.71 kB CSS / gzip 6.71，75 modules）。
>   - **TR-12.2 主链路（t12a，全真实 UI 点击）**：注册→onboarding（选英语/定级路径）→定级 10/10→L3→课程树仅 L1 解锁；L1《见面问候》四环节满分（闪卡 8/8 KNOWN、语法 100、听力 100、口语 RECOGNITION 100）→枢纽 4✅→盖章结算 STAMPED：accuracy 100、base 36+加成 20=**56 XP**、🔥streak 1、学习时长 3 分钟、新成就 first_step+perfect。修复回归中发现的 1 个真实 bug 后，课程中心 UI 加入 ja/ko → **polyglot**；社区真实 UI 发帖（→**social**，合计 4/8 徽章）、点赞 toggle（1→0→1）、盖楼评论 3 条、作者确认框删帖（total 8→7，评论物理级联 0 残留）。
>   - **安全矩阵**：越权删他人帖 **403**「只能删除自己发布的帖子」；未选学语种写课时进度 **403**「请先加入该语种的学习」；已选学但未解锁的 lesson 3 写进度 **403**「请先完成上一节课再解锁本课」（课程树与解锁链同步：L1 完成后 L2 unlocked、L3 locked）；独立探测用户名连续 5 次错密 401、第 6/7 次 **429**「请 10 分钟后再试」，主账号零污染。
>   - **仪表盘/推荐/复习/目标**：GET /dashboard 六组指标对账——Lv1/56 XP/11%、streak 1、今日 56/30 达标/3 分钟/1 课、7 天柱仅今天 56、en 1/12=8% avgAccuracy 100、语法/听力/口语均分 100（词汇取掌握率，首刷 stage 2 属 learning/mastered=0 为 SRS 设计）、dueToday=0；GET /review/due total=0（词卡 due 10-17）；推荐 CONTINUE_LESSON→/lessons/2《数字与时间》+ACHIEVEMENT_WALL（t12b 新账号则为 TAKE_PLACEMENT/DIRECT_START/SET_GOAL 三条）；/profile 四档切 30→60，UI aria-pressed 与 PATCH 持久化一致。
>   - **重启保持**：固定 `JWT_SECRET` 显式重启（run27→run28）后旧 token 200 存活；DB 全量对账 t12a goal=60/streak=1/en L3+ja+ko/lesson1 accuracy=100/4 徽章/8 词卡全部保留，UI 刷新 /dashboard 不掉登录。（回归期观察：未显式固定密钥的测试进程在宿主 Shell 会话间出现过 JWT 验签漂移；jar 内外 application.yml 密钥串一致，显式配置环境变量后可重复存活，属测试环境问题，非产品缺陷；当前 401/200 响应均带 `Cache-Control: no-store`。）
>   - **双账户零串扰（t12b）**：独立注册/登录后 0 enrollment（课程中心三语均「加入学习」）、Lv1/0 XP/streak 0、课程树全新；社区可读他人帖与全部 3 条评论、可点赞评论，但作者「删除」按钮不可见（越权 API 已 403）；跨账户评论正常落楼。
>   - **390 抽查**：CDP `setDeviceMetricsOverride {390×844, dsf=3, mobile:true}`，/home、/courses、/dashboard、/community、/achievements 五页 scrollWidth==clientWidth=390，零横向溢出。
>   - **本轮修复的真实 bug**：[CourseService.java](file:///d:/VideCoding/temp_20261013_01/server/src/main/java/cn/linguaverse/course/service/CourseService.java) `listLanguages` 原将 enrolled/placementLevel 硬编码为 false/null（与 `courseTree` 行为不一致），已改为经 `enrollmentService.listByUser` 批量构建映射返回真实值；修后重打包 99 用例仍全绿。
>   - **证据**：截图 20 张归档 `_walk/t12-*.png`（定级/四环节/结算/课程中心/仪表盘/复习空态/成就墙 4of8/社区/目标档/390/重启后仪表盘 + t12b 隔离 4 张）；全程应用自身 console 0 error（仅 React Router v7 future flag 警告；网络面板中的 401/400/429 均为本轮安全矩阵的主动探测请求）。TR-12.1/12.2 满足。
>
> - **Task 11/12 测试补强 + 集成测试 + E2E 落地（2026-10-14，自动化测试轨收口）**：
>   - **后端单测 99→122 全绿**（`$env:JAVA_HOME='C:\Program Files\Java\jdk-27'; mvn test`，默认不触发 IT、不依赖 Docker）：新增 [CourseServiceTest.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/course/service/CourseServiceTest.java) 8 用例——其中 `listLanguages` 回归用例锁住 Task 12 修的真实 bug（enrolled/placementLevel 必须来自 `enrollmentService.listByUser`，断言未选学=false/null、选学后=true/定级级别），另覆盖 courseTree 完成/解锁/级别 released 映射、课时详情 404（不存在/未发布）/403（未选学/未解锁）、详情 JSON 不含 answerIndex；新增 Web 切片 [QuizApiSliceTest.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/course/QuizApiSliceTest.java) 8 用例（真实 SecurityConfig + GlobalExceptionHandler：无 token 401、已认证畸形 JSON 400、空答案/负下标 400、业务 403/404/400、200 信封）与 [AuthApiSliceTest.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/user/AuthApiSliceTest.java) 7 用例（注册空用户名/非法字符/短密码/畸形 JSON 400、重复 409、成功 200、登录 429）。切片支撑：[JwtFilterMockConfig.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/support/JwtFilterMockConfig.java)（mock JwtAuthFilter，匿名/认证两种放行）+ [SliceMappersMockConfig.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/support/SliceMappersMockConfig.java)（@MapperScan 在切片仍注册 21 个 Mapper 而切片无 SqlSessionFactory，以同名 mock 顶替，需 `spring.main.allow-bean-definition-overriding=true`）。要点：Spring Security 授权先于参数解析，匿名访问受保护端点的畸形 JSON 一律 401（400 用例必须携带合法 token）。
>   - **集成测试（Testcontainers，恢复条件满足后落地）**：[pom.xml](file:///d:/VideCoding/temp_20261013_01/server/pom.xml) 新增 testcontainers-bom 1.20.4 + mysql/junit-jupiter（test scope）与 maven-failsafe-plugin（收在 `<profile id="it">`，默认 `mvn test/package` 不触发）；[CoreFlowIT.java](file:///d:/VideCoding/temp_20261013_01/server/src/test/java/cn/linguaverse/integration/CoreFlowIT.java) 2 用例 `mvn -Pit failsafe:integration-test failsafe:verify` 全绿（mysql:8.0 容器 ~20s 起）：① schema.sql 二次执行 + ContentSeeder 二次灌种双幂等（3 语种/36 课时/288 词计数不变）；② 注册真实用户→定级 10/10 直读题库正确答案→L3 enrolled→课程树仅 seq1 解锁→补四环节满分→结算 56XP/accuracy100/streak1/first_step+perfect→seq1 completed 且 seq2 unlocked→重复结算 alreadyCompleted 幂等（XP 从流水还原、不重发成就）→SRS stage4 评 KNOWN 进 mastered 退出 due 队列、新评 UNKNOWN 当天入队。环境坑已固化：Docker Engine 29（Desktop 4.52+）最低 API 1.44，Testcontainers 1.20.x 默认协商版本被 400 拒绝，[src/test/resources/docker-java.properties](file:///d:/VideCoding/temp_20261013_01/server/src/test/resources/docker-java.properties) 锁 `api.version=1.44`；`@SpringBootTest(webEnvironment=NONE)` 下 SecurityConfig 无法装配 HttpSecurity，IT 用 RANDOM_PORT。
>   - **前端单测基座（Task 2.6 落地）**：vitest 5 + jsdom + @vitest/coverage-v8（devDependencies），[vite.config.ts](file:///d:/VideCoding/temp_20261013_01/client/vite.config.ts) 加 test 段（jsdom/globals/include），`npm run test|test:run|test:coverage`；[api.test.ts](file:///d:/VideCoding/temp_20261013_01/client/src/lib/api.test.ts) 8 用例（code=0 解包并带 Bearer、无 token 不发 Authorization、断网/在线不可达两种 ApiError(0) 文案、401 清会话跳 /login、登录页 401 不跳转、非 JSON 502、业务 code!=0 透传）+ [session.test.ts](file:///d:/VideCoding/temp_20261013_01/client/src/lib/session.test.ts) 3 用例（zustand persist lv-session 写入/清除、初始未登录），`npm run test:run` **11 用例全绿**；`npx tsc --noEmit` 零错误（e2e/ 不在 tsconfig include，不影响 `npm run build`）。
>   - **E2E（Playwright，恢复条件满足后落地）**：`@playwright/test` + chromium 仅 devDependencies；[playwright.config.ts](file:///d:/VideCoding/temp_20261013_01/client/playwright.config.ts) 双 webServer 自动拉起 Vite（127.0.0.1:5173，CORS 白名单端口）与 `mvn spring-boot:run`（cwd ../server，注入 JDK27，健康检查 /api/health，已运行则复用），`npm run e2e`；[e2e/auth.spec.ts](file:///d:/VideCoding/temp_20261013_01/client/e2e/auth.spec.ts) 2 条（注册→onboarding 且刷新保持登录；错密 5 次 401 文案、第 6 次 429「登录失败次数过多」）+ [e2e/learning-chain.spec.ts](file:///d:/VideCoding/temp_20261013_01/client/e2e/learning-chain.spec.ts) 1 条（注册→定级 10 题→课程地图→L1 闪卡 8/8→语法/听力/口语（headless 走 SELF 流利档）→盖章 STAMPED 显示 +XP 与初学乍练→地图出现 1 个已完成站点），账号 `e<tag><时间戳36进制>` 唯一隔离。实测 **3 条 spec 全绿（~52s，含后端启动）**。踩坑记录：Vite 默认只绑 IPv6 `[::1]`，Chromium 间歇走 IPv4 拒连 → 强制 `--host 127.0.0.1` 并用 127.0.0.1 作 baseURL；听力页首个 section.card 是「播放对话」，选项按钮需按名字 `^A\S` 定位；「认识」按钮匹配需 exact 避免命中「不认识」；课时详情异步加载，进入子页后先等题目渲染再操作。
>   - **最终口径**：后端单测 122（12 类）+ IT 2（Testcontainers）+ 前端 Vitest 11 + E2E 3 = **138 个自动化用例全绿**；`mvn test` 仍零外部依赖（无 Docker/DB），IT 仅在 `-Pit` 触发，前端测试依赖全部不进 build 产物。
>
> 下列 Task 1–12 的 Status 视为 `done`。

> **测试补强并行轨（2026-10-14 增补，仅单测先行）**：后端已推进至 Task 8、前端在 Task 2.5 页面批次中。为不影响在途任务，测试以独立并行任务插入：Task 2.6（前端 Vitest 单测基座）、Task 8.5（后端核心规则单测）。原则：
> 1. **不改门禁**：各任务既有的手动实测/走查 TR 仍是 MVP 验收依据；本轨仅补自动化单测，不是 Task 8–12 的阻塞前置；
> 2. **零运行时影响**：后端测试依赖仅 test scope，`mvn test`/`mvn package` 不需要数据库与 Docker；前端测试依赖仅 devDependencies，不进 build 产物；
> 3. **先后顺序**：先锁已稳定且易回归的规则（定级阈值、SRS 间隔、判题分、XP/Streak、JWT/限流），Task 9/10 新增规则随行补测，不回头返工。
>
> **暂缓事项（集成测试与 E2E）——均已于 2026-10-14 落地，见上方「Task 11/12 测试补强」执行记录**：
> - **集成测试**（`@WebMvcTest` 错误码矩阵切片、Testcontainers MySQL 的 `*IT.java` + `-Pit` profile）：恢复条件（Task 9/10 完成、接口面收敛）已满足。已落地：QuizApiSliceTest 8 + AuthApiSliceTest 7 固化 400/401/403/404/409/429 矩阵与答案不泄露；CoreFlowIT 2 例固化灌种双幂等、解锁链、满分结算与重复结算幂等、SRS mastered 出队。failsafe 收在 `it` profile，默认 `mvn test` 不触发。
> - **E2E**（Playwright 主链路自动化）：恢复条件（Phase 2 完成、回归轮次增多）已满足。已落地 3 条主链路 spec（认证 2 + 学习主链路 1），`npm run e2e` 自动拉起前后端，唯一后缀账号隔离；依赖仅 devDependencies。

## Task 1: 服务端基座（MySQL、Spring Security、契约骨架）

- **Status**: `done`
- **Priority**: high
- **Depends On**: Phase 0 完成；JDK 17、MySQL 8、空库 `linguaverse` 就绪
- **Description**: Maven 工程（web/security/validation/mybatis-plus/mysql-j/jjwt/lombok）；`schema.sql` 幂等建表（users/enrollments/placement_results/lesson_completions/lesson_stage_progress/word_mastery/activities/posts/comments/post_likes/achievement_unlocks + 课程内容表，索引与外键、级联删除）；Security 无状态过滤链 + JWT 过滤器 + BCrypt；统一响应体、全局异常、Validation、分页插件、登录限流拦截器；`GET /api/health`；CORS 允许 :5173；CONTRACT.md 建档。
- **Acceptance Criteria Addressed**: NFR-1/4/8/9/10/12、AC-15
- **Test Requirements**:
  - `rule` TR-1.1: 空库启动自动建表，二次启动无报错；无 token 访问受保护端点 401；超阈值登录 429，证据为启动日志/SHOW TABLES/HTTP 记录。
  - `rule` TR-1.2: `mvn package` 零错误产出 jar，证据为命令输出。

## Task 2: 课程内容 JSON 种子器

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 1
- **Description**: resources/content JSON 三语种 L1–L3（36 课时四环节真实语料）+ L4–L6 upcoming + 每语种定级题 10 题；ApplicationRunner 幂等灌种与字段自检；社区种子帖/评论；XP/等级/8 成就/每日目标枚举。
- **Acceptance Criteria Addressed**: AC-3、AC-U2
- **Test Requirements**:
  - `rule` TR-2.1: 自检零错误、重启不重复灌种，证据为启动日志与行数对比。
  - `rubric` TR-2.2: 语料质量（对齐 AC-U2）；scale 1-5；threshold >= 4；证据为三语抽样走查。

## Task 2.5: client/ React 工程迁移与 53 视图组件化

- **Status**: `done`
- **Priority**: high
- **Depends On**: Phase 0 原型完成（`prototype/` 53 视图走查通过）
- **Description**: Phase 0 实际交付为纯静态 HTML，本任务补齐原计划 P0-1 未落地的工程化部分，是 Task 3–10 全部"前端接真"条目的共同前置：
  - 搭建 `client/` Vite + React 18 + TS 严格工程（Tailwind v4、React Router、Zustand、lucide-react）；引入字体（Fraunces、Noto Serif/Sans SC/JP/KR）。
  - 设计令牌迁移：`prototype/assets/passport.css` 的 Paper/Ink/Passport/Stamp/Visa Gold/Thread 与三语种色、字体阶梯、纸线/纸叠投影 → Tailwind 主题与语义组件类。
  - 基础组件迁移：邮票齿孔卡（PerforatedCard）、签证贴纸（VisaSticker）、圆邮戳（RubberStamp 含盖章动画）、目标环、路线站点、纸质感按钮 primary/ghost/danger、输入/校验态、Toast、Modal、分段进度、空状态/加载骨架/错误重试、阶段角标（M/1/2）。
  - 两套外壳与公共交互迁移：学员端（顶部导航+移动底部导航）、管理后台端（侧边栏）；`prototype/assets/app.js` 的翻卡、toast、modal、计时等交互以 React 重写。
  - 53 视图全部迁移为路由页面（结构/真实语料/状态与静态原型一致），保留 `/archive` 索引页直达全部页面。
  - mock 数据层 `src/mocks/` + 统一 API 接口层：接口签名对齐 `server/CONTRACT.md`，接真时仅替换实现（`VITE_API_BASE` 默认 `http://localhost:8080/api`，本机联调临时用 `18080`），并预留 401 拦截回跳登录页。
- **Acceptance Criteria Addressed**: AC-16、AC-U1
- **Test Requirements**:
  - `rule` TR-2.5.1: `npm run dev` 可访问，`/archive` 计数为 53 且全部视图可达，证据为走查记录。
  - `rule` TR-2.5.2: `npm run build` 零错误；390/768/1440 三宽度外壳与页面无横向溢出，证据为构建输出与截图。
  - `rule` TR-2.5.3: 与 `prototype/` 静态页面逐页对比走查，关键交互（翻卡/盖章/toast/modal/计时）行为一致，控制台无 error，证据为对比截图与控制台输出。
  - `rubric` TR-2.5.4: "语言护照"材质还原度（纸/邮戳/齿孔/签证）；scale 1-5；anchors 1=通用模板，3=整洁普通，5=与静态原型无可见差异；threshold >= 4；证据为组件廊截图。

## Task 2.6: client 单测基座（Vitest）

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task 2.5（工程基座）；**不阻塞在途页面批次，可并行**
- **Description**:
  - 仅新增 devDependencies：`vitest`、`jsdom`、`@vitest/coverage-v8`；vite.config.ts 增 test 段（environment jsdom），package.json 增 `test`/`test:run`/`coverage` 脚本，测试文件不进 build 产物。
  - 首批用例（纯函数/工具层）：`lib/session.ts` token 存取与清理、`lib/api.ts` 统一错误 envelope 解析与 401 拦截回跳、`lib/routes.ts` 路由表完整性。
  - 随行约定：Task 3–10 接真批次中新产生的纯逻辑（store、判分/计时/进度等纯函数）随行补单测；页面交互测试已由 Playwright E2E 承接（2026-10-14 落地，见 Task 12 执行记录），不再暂缓。
- **Acceptance Criteria Addressed**: NFR-1
- **Test Requirements**:
  - `rule` TR-2.6.1: `npm run test:run` 全绿；`npm run build` 产物与耗时无可见变化，证据为命令输出。
  - `rule` TR-2.6.2: 401 回跳与 session 持久化有断言覆盖，人为改坏断言时测试变红，证据为用例清单。

## Task 3: 认证、Onboarding、资料与前端对接

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 2、Task 2.5
- **Description**: register/login/me、PATCH /me（含 daily_goal）、stats、onboarding 端点；前端 API 基座替换 mock（401 拦截回跳）；P01–P05、P40、P42 接真。
- **Acceptance Criteria Addressed**: AC-1、AC-2、AC-17、AC-18（设置）
- **Test Requirements**:
  - `rule` TR-3.1: 注册→登录→刷新保持→退出→错密拒绝→未登录回跳全链路；onboarding 三路径落库；DB 无明文密码，证据为走查 + DB。

## Task 4: 课程中心、定级、解锁与对接

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 3
- **Description**: courses 树、enrollments、placement 判分、lesson 详情（403 锁定校验，答案不下发）；P09–P12 接真。
- **Acceptance Criteria Addressed**: AC-3、AC-4、AC-5、NFR-12
- **Test Requirements**:
  - `rule` TR-4.1: 极端定级结果不同并持久化；未解锁课时 API 403；L4+ 无内容出口，证据为 HTTP/UI/DB。

## Task 5: 单词闪卡/SRS 与复习对接

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 4
- **Description**: lesson state/stage、vocab review（SRS-lite 档位/到期）、due 队列；P14、P19 接真。
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `rule` TR-5.1: 三标记产生不同 stage/dueAt 且排序正确；进度退出可恢复，证据为 HTTP/DB/走查。

## Task 6: 语法、听力、口语判题接口与对接

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 5
- **Description**: grammar/listening/speaking submit（判分解析、成绩入 activities、speaking 兼容识别分/自评分）；P15–P17 接真。
- **Acceptance Criteria Addressed**: AC-7、AC-8、AC-9
- **Test Requirements**:
  - `rule` TR-6.1: 三类判题返回正确解析并入库；课时详情响应无答案字段；口语两路径至少其一可用，证据为 Network/DB/走查。

## Task 7: 课时结算（事务/XP/Streak/成就）与对接

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 6
- **Description**: lesson complete 事务（幂等完成、XP 明细、durationSec、Streak、成就评估返回）；P18 接真。
- **Acceptance Criteria Addressed**: AC-10、AC-14（部分）
- **Test Requirements**:
  - `rule` TR-7.1: 结算数值与作答一致、下一课解锁、重复完成不重发奖，证据为走查 + DB。

## Task 8: 仪表盘、每日目标与推荐引擎对接

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 7
- **Description**: dashboard 聚合（含今日分钟/目标）；recommendations/today 规则引擎（含达标鼓励文案）；P06、P26 接真。
- **Acceptance Criteria Addressed**: AC-11、AC-12、AC-18
- **Test Requirements**:
  - `rule` TR-8.1: 6 类指标与 DB 一致；新老两种状态推荐不同且 CTA 直达；切档持久化，证据为走查/HTTP。
  - `rubric` TR-8.2: 推荐相关感（AC-U3）；scale 1-5；threshold >= 4；证据为多状态走查。

## Task 8.5: 后端单测基座与 Task 1–8 核心规则自动化回归

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task 7（核心规则已稳定）；**可与 Task 8/9/10 并行，不是其前置**
- **Description**: JUnit 5 + Mockito + AssertJ（均已含于 spring-boot-starter-test，零新依赖），Mapper 全部 mock、不连数据库：
  - 定级边界（PlacementService）：0 对→L1、恰好 4 对→L2（阈值 0.4）、全对→L3；选项越界/题数不符→400。
  - SRS（SrsService）：UNKNOWN→stage0 当天到期；VAGUE→stage+1 次日；KNOWN→stage+2 三天后；stage≥6 mastered 退出队列；due limit 默认 50、上限 100。
  - 判题（QuizService）：语法全对 100/半对 50；听力对 100 错 0；口语 RECOGNITION 归一化 Levenshtein（全等 100、全异趋近 0）与 SELF 三档 60/80/95；混入他课题目/越界下标→400。
  - 结算（CompletionService）：XP=词数×2+四环节×5、bonus=round(accuracy%×20)、四环节均值取整；Streak（昨天+1/同日不增/断签重置 1）；缺环节→400；重复结算幂等不重发 XP/成就。
  - 安全（JwtService/LoginRateLimiter）：签发→解析往返、过期/篡改拒绝；错密 5 次后锁定、锁定窗口过后放行。
  - 随行约定：Task 8 收尾及 Task 9/10 新增规则（推荐引擎、社区交互、徽章评估器）同任务补纯单测，不另开返工。
- **Acceptance Criteria Addressed**: NFR-10、AC-15（辅助）
- **Test Requirements**:
  - `rule` TR-8.5.1: `mvn test` 全绿且不依赖 MySQL/Docker，证据为输出与用例清单。
  - `rule` TR-8.5.2: 变异抽查——各改定级阈值/SRS 间隔/XP 公式 1 处均有对应用例变红，证据为抽查记录。

## Task 9: 社区端点与对接

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task 3
- **Description**: posts 列表（分页契约）、发帖、点赞 toggle、评论、删除（仅作者 403 校验、级联）；P32/P33 接真。
- **Acceptance Criteria Addressed**: AC-13
- **Test Requirements**:
  - `rule` TR-9.1: 五类交互持久化且越权删除 403，证据为走查 + DB。
  - `rule` TR-9.2: 随行补纯单测：发帖/点赞 toggle/评论/删除分支与越权 403 判定（Mapper 全 mock），证据为 `mvn test` 输出。

## Task 10: 成就系统与对接

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Task 7、Task 8、Task 9
- **Description**: 8 徽章评估器（唯一约束防重）、achievements 列表（进度）；P27 接真；弹层事件贯通。
- **Acceptance Criteria Addressed**: AC-14
- **Test Requirements**:
  - `rule` TR-10.1: 单会话真实触发 4 枚徽章且重复动作不重发，证据为走查 + DB。
  - `rule` TR-10.2: 8 枚徽章评估器逐条件纯单测（边界触发 + 重复动作不重发），证据为 `mvn test` 输出。

---

# Phase 2：打磨、构建与端到端回归

## Task 11: 沉浸式打磨、响应式与无障碍

- **Status**: `done`
- **Priority**: medium
- **Depends On**: Phase 1 全部
- **Description**: 在真实数据下复查全部 MVP 视图；空/错/加载三态；断网提示；390/768/1440；键盘焦点、aria、对比度；动效减法终检。
- **Acceptance Criteria Addressed**: AC-16、AC-U1、NFR-2/3/6/11
- **Test Requirements**:
  - `rule` TR-11.1: 三宽度 × MVP 六类页面无破版；键盘完成登录→答题→点赞；断网友好提示，证据为截图矩阵与走查。
  - `rubric` TR-11.2: 沉浸品质终检；scale 1-5；threshold >= 4；证据为截图集。

## Task 12: 双端构建与 E2E 回归

- **Status**: `done`
- **Priority**: high
- **Depends On**: Task 11
- **Description**: `mvn package` + `npm run build` 零错误；E2E：落地页→注册→onboarding→定级→完整课时（计时）→结算徽章→仪表盘 6 指标→到期复习/推荐变化→切目标档→社区全交互（403/429）→4 徽章成就墙→刷新/重启保持→双账户隔离→390 抽查；沉淀证据。
- **Acceptance Criteria Addressed**: AC-15、AC-2、全部 AC 回归
- **Test Requirements**:
  - `rule` TR-12.1: 双构建退出码 0，jar 可启动，证据为输出。
  - `rule` TR-12.2: E2E 清单全通过、控制台无 error；重启 Spring Boot 数据保留、双账户零串扰，证据为回归记录与截图。
  - `rule` TR-12.3（2026-10-14 增补，已完成）: Playwright 自动化 E2E 已落地并全绿——认证链路 2 条 + 学习主链路 1 条（`npm run e2e` 双 webServer 自动拉起前后端，~52s），TR-12.2 手动清单之外补充的回归资产，详见执行记录「Task 11/12 测试补强 + 集成测试 + E2E 落地」。
