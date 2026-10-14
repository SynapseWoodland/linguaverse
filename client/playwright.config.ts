import { defineConfig, devices } from '@playwright/test'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

/**
 * E2E（Task 10.5）：真实前后端 + 真实 MySQL（本机 linguaverse-mysql:3306）。
 * - 前端 Vite 固定 5173（后端 CORS 白名单端口），后端 mvn spring-boot:run（JDK 27）健康检查 /api/health
 * - 已在运行的服务会被复用（reuseExistingServer）
 * - 仅 devDependencies，不进 build 产物；账号用唯一后缀隔离
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1,
  timeout: 90_000,
  reporter: process.env.CI ? 'line' : [['list'], ['html', { open: 'never' }]],
  use: {
    // 用 127.0.0.1 避免 localhost 解析到 IPv6/IPv4 与 Vite 监听地址不一致导致间歇拒连
    baseURL: 'http://127.0.0.1:5173',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command: 'npm run dev -- --host 127.0.0.1 --port 5173 --strictPort',
      url: 'http://127.0.0.1:5173/',
      reuseExistingServer: true,
      timeout: 90_000,
    },
    {
      command: 'mvn spring-boot:run',
      cwd: path.resolve(__dirname, '../server'),
      url: 'http://localhost:8080/api/health',
      reuseExistingServer: true,
      timeout: 300_000,
      env: {
        ...process.env,
        // 本机默认 JAVA_HOME 指向 JDK 8，主代码需 17+
        JAVA_HOME: 'C:\\Program Files\\Java\\jdk-27',
        PATH: `${process.env.PATH ?? ''};C:\\Program Files\\Java\\jdk-27\\bin`,
      },
    },
  ],
})
