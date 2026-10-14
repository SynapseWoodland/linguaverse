English | [简体中文](README.zh-CN.md)

<div align="center">

<img src="docs/assets/banner.png" alt="LinguaVerse hero" width="100%" />

# LinguaVerse

**A multilingual learning platform built around a "Language Passport".**
From placement tests to listening / speaking / reading / writing training,
with spaced-repetition review, a learner community and an achievement system —
turn the progress of every language into stamps you collect.

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

## ✨ Why LinguaVerse

> Not just another flashcard app — a complete learning loop:
> **find your level → plan what to learn → train all four skills → reinforce with SRS → stay with community & achievements.**

| 🧭 Placement | 🎯 Learning path | 🔤 Four skills | ♻️ Spaced review | 👥 Community | 🏆 Achievements |
| :--: | :--: | :--: | :--: | :--: | :--: |
| locate your level | course-map guidance | listen / speak / read / write | SRS flashcards | posts & comments | badges & goals |

---

## 🧰 Tech Stack

| Layer | Choices |
| --- | --- |
| Frontend `client/` | React 18 · Vite 6 · TypeScript 5.7 · Tailwind CSS 4 · Zustand · React Router 6 |
| Backend `server/` | Spring Boot 3.5 (Java 17) · MyBatis-Plus · Spring Security + JJWT · MySQL 8 |
| Testing | Vitest + jsdom · Playwright (E2E) · JUnit 5 + Testcontainers (integration) |
| Prototype `prototype/` | Static high-fidelity HTML prototype (design reference, not production code) |

---

## 🚀 Quick Start

**Prerequisites:** Node 24.x · JDK 17 (matching `server/pom.xml`) · local MySQL (database `linguaverse`)

```bash
# 1) Backend — port 8080, common prefix /api
cd server
mvn spring-boot:run

# 2) Frontend — port 5173, /api proxied to http://127.0.0.1:8080 by Vite
cd client
npm ci
npm run dev

# Optional: static prototype preview
node _prototype-server.js
```

In a hurry? Start backend + frontend with one command (Windows PowerShell):

```powershell
powershell -File scripts/start-all.ps1
```

Open <http://localhost:5173>. All frontend requests go through the relative path `/api` (see `server.proxy` in `client/vite.config.ts`), so no backend URL is hard-coded.

---

## 🧪 Testing

```bash
# Frontend: type check / unit tests / production build
cd client
npx tsc --noEmit
npm run test:run
npm run build

# Backend: unit tests (no Docker required)
cd server
mvn -B test

# Backend: integration tests (Docker required, MySQL via Testcontainers)
cd server
mvn -B verify -Pit
```

CI is defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml): path filters on `client/**` and `server/**` run only the affected jobs.

---

## 🗂 Project Structure

```
.
├── client/          # React + Vite frontend (src/{pages,components,lib,styles})
├── server/          # Spring Boot backend (src/main/java/cn/linguaverse/**)
│   └── CONTRACT.md  # Backend API contract: response / auth / status codes / CORS
├── prototype/       # Static high-fidelity prototype
├── docs/assets/     # README visual assets
├── scripts/         # Dev helper scripts (start-all.ps1, ...)
└── .github/         # CI workflows
```

## 📚 Docs & Conventions

- Product spec / design plan / task breakdown: `.trae/specs/multilingual-learning-platform/`
- Backend API contract: [`server/CONTRACT.md`](server/CONTRACT.md)
- Entry point & conventions for AI coding agents: [`AGENTS.md`](AGENTS.md)
- Version control: this repo is under Git — use `git status` / `git diff` to bound changes and keep a rollback point.

---

## 🤝 Contributing

Issues and Pull Requests are welcome. For substantial changes, please open an issue first to align on direction, and make sure the affected module's local tests pass (see [Testing](#-testing)) and the conventions in `AGENTS.md` are followed.

## 📄 License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for details.
