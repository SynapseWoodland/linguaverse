import { test, expect } from '@playwright/test'
import { registerViaUi, uniqueUser } from './utils'

test.describe('A · 认证链路', () => {
  test('注册成功 → 进入入门引导；会话持久化，直接访问受保护页不回登录页', async ({ page }) => {
    const { username } = uniqueUser('reg')
    await registerViaUi(page, username)

    // zustand persist：刷新后仍保持登录态
    await page.reload()
    await expect(page).toHaveURL(/\/onboarding/)

    await page.goto('/home')
    await expect(page).not.toHaveURL(/\/login/)
    await expect(page.getByText('今日计划').or(page.getByText('学习首页'))).toBeVisible()
  })

  test('错误密码给出提示；连续 5 次失败后第 6 次被 429 锁定', async ({ page }) => {
    const { username } = uniqueUser('lck')
    await registerViaUi(page, username)

    await page.goto('/login')
    const submit = page.getByRole('button', { name: /登录，继续学习/ })

    for (let i = 1; i <= 5; i++) {
      await page.fill('#login-username', username)
      await page.fill('#login-password', `wrong-pwd-${i}`)
      await submit.click()
      // 401 经 apiFetch 统一拦截（登录页不跳转），展示统一过期文案
      await expect(page.getByText('未登录或登录已过期').first()).toBeVisible()
    }

    // 第 6 次：失败计数已达上限，限流在鉴权前直接返回锁定提示
    await page.fill('#login-password', 'wrong-pwd-6')
    await submit.click()
    await expect(page.getByText('登录失败次数过多').first()).toBeVisible()
  })
})
