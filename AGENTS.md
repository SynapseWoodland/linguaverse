# AGENTS.md — LinguaVerse 编码代理入口

本文件是进入本仓库的**代理/新人第一入口**：只做路由，指向已有的权威上下文，不复制其内容。

## 项目定位

LinguaVerse 是多语言学习平台，仓库为三段式结构：

| 模块 | 技术栈 | 入口 |
| --- | --- | --- |
| `client/` | React 18 + Vite 6 + TypeScript + Tailwind 4 | `client/src/main.tsx` → `client/src/router.tsx` |
| `server/` | Spring Boot 3.5 / **Java 17** + MyBatis-Plus + JJWT | `server/src/main/java/cn/linguaverse/**` |
| `prototype/` | 静态 HTML 原型（无后端） | `prototype/index.html`、`prototype/pages/*.html` |

## 权威上下文（改动前先读）

- 需求规格：`.trae/specs/multilingual-learning-platform/spec.md`
- 设计方案：`.trae/specs/multilingual-learning-platform/design-plan.md`
- 任务拆解：`.trae/specs/multilingual-learning-platform/tasks.md`
- 后端接口契约：`server/CONTRACT.md`（统一响应、鉴权、状态码、CORS）
- 前端 API 客户端：`client/src/lib/api.ts`；会话/存储：`client/src/lib/session.ts`

## 模块边界与约定

- 前端请求统一走相对路径 `/api`，由 Vite dev server 代理到后端（见 `client/vite.config.ts` 的 `server.proxy`）。不要在组件里写死后端绝对地址。
- 后端接口以 `server/CONTRACT.md` 为准：`Authorization: Bearer <JWT>`、`/api` 前缀、`{ code, message, data }` 统一响应。
- 原型目录 `prototype/` 是设计参考资产，非生产代码；`_prototype-server.js` 仅用于本地静态预览。

## 如何运行（本机，PowerShell）

前置：**Node 24.x**、**JDK 17**（与 `server/pom.xml` 的 `java.version=17` 一致）、本地 MySQL（库名 `linguaverse`，见 IDE 数据源）。

```powershell
# 后端（默认端口 8080，前缀 /api）
cd server; mvn spring-boot:run

# 前端（默认端口 5173，/api 代理到 127.0.0.1:8080）
cd client; npm ci; npm run dev

# 原型静态预览（可选）
node _prototype-server.js
```

一键拉起见 `scripts/start-all.ps1`（仅顺序启动后端 + 前端并打印端口/失败信息，不引入额外运行时）。

## 如何校验（改动后必跑，与 CI 对齐）

CI 用 `client/**`、`server/**` 路径过滤只跑受影响作业（见 `.github/workflows/ci.yml`）。本地等价复现：

```powershell
# 前端：类型检查 + 单测 + 构建
cd client; npm run test:run; npx tsc --noEmit; npm run build

# 后端：单元测试（不依赖 Docker）
cd server; mvn -B test

# 后端：集成测试（可选，需 Docker，走 Testcontainers 起 MySQL）
cd server; mvn -B verify -Pit
```

## 版本控制

本仓库已纳入 Git（见根 `.gitignore`）。改动前用 `git status` / `git diff` 界定范围，保留可回滚的提交点。生成物、scratch 日志、`.idea/`、`.qoder/`、`server/target/` 已被忽略。
