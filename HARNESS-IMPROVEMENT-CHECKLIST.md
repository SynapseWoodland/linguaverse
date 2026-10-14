# LinguaVerse Harness 优化实施 Checklist

> 依据 better-harness 复核的 4 项发现制定。勾选反映**当前真实状态**：
> `[x]` = 已完成并校验；`[ ]` = 待办（多为需要你授权的写入/外部操作）。
> 更新时间：2026-10-14

## 关联发现与维度
- 基线得分：任务理解 45 / 可控执行 52 / 改动验证 55 / 可靠交付 30 / 经验沉淀 38（未观察）
- F1[中] 无版本控制/回滚边界 · F2[低] 指引未路由 · F3[低] 无统一启动路线 · F4[低] 测试无改动闸口 · F5 证据缺口（保持边界内）

---

## 阶段 1 · 版本控制基线（F1，可靠交付/范围界定）
- [x] 初始化 Git 仓库（`git init`）
- [x] 根 `.gitignore`（忽略 `node_modules/`、`client/dist/`、`server/target/`、`*.log`、`_server-run*.log`、`.idea/`、`.qoder/`、`QODER-SESSION-CHANGES.diff`）
- [x] `.gitattributes`（统一行尾，保证 `git diff` 只反映真实改动）
- [x] 验证忽略规则（被忽略项 0 个进入暂存；`_walk/` 按计划纳入）
- [ ] **执行基线提交**（需授权）：`git commit -m "chore: baseline commit for harness safety"`
  - ⚠️ 未完成此项前，"可回滚点 + diff 界定范围"尚未真正闭合

## 阶段 2 · 根级可路由指引（F2，任务理解/相关上下文）
- [x] `AGENTS.md`（代理第一入口：定位 / 权威上下文指针 / 模块边界 / 运行 / 校验）
- [x] `README.md`（英文默认）
- [x] `README.zh-CN.md`（中文），顶部双语切换：`English | 简体中文`
- [x] 链接指向 `.trae/specs/...`、`server/CONTRACT.md`、`AGENTS.md`（路径逐一核对存在）
- [x] `LICENSE`（MIT）+ 恢复 License 徽章
  - [ ] 可选：把版权署名 `LinguaVerse Contributors` 换成你的名字/组织
  - [ ] 可选：改用 Apache-2.0 / GPL-3.0（当前为 MIT）

## 阶段 3 · 统一启动/复位路线（F3，可控执行/可复现启动）
- [x] 文档记录三模块启动命令与前置（Node 24.x / JDK 17 / 本地 MySQL `linguaverse`）
- [x] 校正真实端口：后端 8080、前端 5173（`client/vite.config.ts` proxy → `127.0.0.1:8080`；`.env.development` 中"18080"注释为过时）
- [x] `scripts/start-all.ps1` 一键拉起后端 + 前端（不新增运行时假设）
- [ ] 从干净状态按文档实测一次拉起，记录实际命令/任一阻塞点

## 阶段 4 · 改动后校验闸口 GitHub Actions（F4，改动验证）
- [x] `.github/workflows/ci.yml`（push / PR / workflow_dispatch 触发）
- [x] `dorny/paths-filter@v3` 按 `client/**`、`server/**` 只跑受影响作业
- [x] frontend job：`npm ci` → `tsc --noEmit` → `npm run test:run` → `npm run build`（Node 24）
- [x] backend job：`mvn -B test`（temurin JDK 17）
- [x] backend-it job：`mvn -B verify -Pit`（Docker/Testcontainers，默认 `continue-on-error`）
- [x] YAML 解析校验通过（作业/触发/needs/if 正确）
- [ ] 推送仓库到 GitHub（Actions 生效前提，需远程地址 + 授权）
- [ ] 决策：`backend-it` 设为必过（移除 `continue-on-error`）还是保持非阻断
- [ ] 首次推送后开一个只改 `client/**` 的 PR，验证只触发 frontend job

---

## 收尾 / 方法学
- [ ] 执行基线提交后，视为 F1 完全闭合
- [ ] （可选）发起一次独立 post-fix 修复复核，把 F1–F4 的 verified 状态固化进 better-harness 报告
- [ ] 经验沉淀（F5）：待后续 ≥2 个可比任务片段积累后重新运行 `/better-harness` 复评

## 当前 git 状态快照
- 分支 `master`，**尚无提交**（UHB：unborn HEAD）
- 已暂存：289 个文件，含全部 harness 产物（`AGENTS.md` `README.md` `README.zh-CN.md` `LICENSE` `.gitignore` `.gitattributes` `scripts/start-all.ps1` `.github/workflows/ci.yml` `docs/assets/banner.png`）
