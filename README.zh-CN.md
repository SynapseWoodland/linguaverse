[English](README.md) | 简体中文

<div align="center">

<img src="docs/assets/banner.png" alt="LinguaVerse 主视觉" width="100%" />

# LinguaVerse

**以「语言护照」为主线的多语言学习平台** —— 从分级测评到听 / 说 / 读 / 写四类训练，
配合间隔重复复习、学习社区与成就体系，把每一门语言的进度盖成一枚枚可收集的印章。

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)](server/pom.xml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?logo=springboot&logoColor=white)](server/pom.xml)
[![React](https://img.shields.io/badge/React-18-61dafb?logo=react&logoColor=white)](client/package.json)
[![Vite](https://img.shields.io/badge/Vite-6-646cff?logo=vite&logoColor=white)](client/package.json)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178c6?logo=typescript&logoColor=white)](client/package.json)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479a1?logo=mysql&logoColor=white)](server/src/main/resources/db/schema.sql)
[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088ff?logo=githubactions&logoColor=white)](.github/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

</div>

---

## ✨ 为什么是 LinguaVerse

> 不是又一门「背单词 App」，而是一条完整的学习闭环：
> **测一测你在哪 → 规划该学什么 → 四类技能轮训 → 间隔重复巩固 → 社区与成就把你留住。**

| 🧭 分级测评 | 🎯 学习路径 | 🔤 四类技能 | ♻️ 间隔重复 | 👥 学习社区 | 🏆 成就体系 |
| :--: | :--: | :--: | :--: | :--: | :--: |
| 定位当前水平 | 课程地图引导 | 听 / 说 / 读 / 写 | SRS 复习卡 | 发帖与互评 | 徽章与目标 |

---

## 🧰 技术栈

| 层 | 选型 |
| --- | --- |
| 前端 `client/` | React 18 · Vite 6 · TypeScript 5.7 · Tailwind CSS 4 · Zustand · React Router 6 |
| 后端 `server/` | Spring Boot 3.5 (Java 17) · MyBatis-Plus · Spring Security + JJWT · MySQL 8 |
| 测试 | Vitest + jsdom · Playwright（E2E）· JUnit 5 + Testcontainers（集成） |
| 原型 `prototype/` | 静态 HTML 高保真原型（设计参考，非生产代码） |

---

## 🚀 快速开始

**前置条件：** Node 24.x · JDK 17（与 `server/pom.xml` 一致）· 本地 MySQL（库名 `linguaverse`）

```powershell
# 1) 后端 —— 端口 8080，统一前缀 /api
cd server
mvn spring-boot:run

# 2) 前端 —— 端口 5173，/api 由 Vite 代理到 http://127.0.0.1:8080
cd client
npm ci
npm run dev

# 可选：静态原型预览
node _prototype-server.js
```

嫌麻烦？一条命令拉起后端 + 前端：

```powershell
powershell -File scripts/start-all.ps1
```

打开 <http://localhost:5173> 即可。前端请求统一走相对路径 `/api`（见 `client/vite.config.ts` 的 `server.proxy`），无需写死后端地址。

---

## 🧪 测试

```powershell
# 前端：类型检查 / 单元测试 / 生产构建
cd client
npx tsc --noEmit
npm run test:run
npm run build

# 后端：单元测试（不需要 Docker）
cd server
mvn -B test

# 后端：集成测试（需要 Docker，通过 Testcontainers 启动 MySQL）
cd server
mvn -B verify -Pit
```

CI 见 [`.github/workflows/ci.yml`](.github/workflows/ci.yml)：按 `client/**` 与 `server/**` 路径过滤，仅运行受改动影响的作业。

---

## 🗂 项目结构

```
.
├── client/          # React + Vite 前端（src/{pages,components,lib,styles}）
├── server/          # Spring Boot 后端（src/main/java/cn/linguaverse/**）
│   └── CONTRACT.md  # 后端接口契约：统一响应 / 鉴权 / 状态码 / CORS
├── prototype/       # 静态高保真原型
├── docs/assets/     # README 视觉资源
├── scripts/         # 开发辅助脚本（start-all.ps1 等）
└── .github/         # CI 工作流
```

## 📚 文档与约定

- 产品规格 / 设计方案 / 任务拆解：`.trae/specs/multilingual-learning-platform/`
- 后端接口契约：[`server/CONTRACT.md`](server/CONTRACT.md)
- 面向 AI 编码代理的入口与约定：[`AGENTS.md`](AGENTS.md)
- 版本控制：本仓库已纳入 Git，改动前用 `git status` / `git diff` 界定范围并保留可回滚提交点。

---

## 🤝 贡献

欢迎 Issue 与 Pull Request。较大改动请先开 Issue 讨论方向，提交前请保证：
受影响模块的本地测试通过（见上方「测试」），并遵循 `AGENTS.md` 中的模块边界约定。

## 📄 许可证

本项目基于 **MIT 许可证** 开源，详见 [LICENSE](LICENSE)。
