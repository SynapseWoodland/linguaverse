import type { Page } from '@playwright/test'

/** 唯一账号：用户名上限 16 位，e + 3 位标签 + 时间戳 36 进制 */
export function uniqueUser(tag = 'u'): { username: string; password: string } {
  const username = `e${tag}${Date.now().toString(36)}`.slice(0, 16)
  return { username, password: 'secret123' }
}

/** 通过注册页完成注册，成功后落在 /onboarding（已登录态）。 */
export async function registerViaUi(page: Page, username: string, password = 'secret123') {
  await page.goto('/register')
  await page.fill('#reg-username', username)
  await page.fill('#reg-password', password)
  await page.fill('#reg-confirm', password)
  await page.getByRole('button', { name: '注册并开始' }).click()
  await page.waitForURL('**/onboarding')
}

/** 引导页：欢迎 → 选英语（默认） → 开始定级测试，落在 /placement。 */
export async function startPlacement(page: Page) {
  await page.getByRole('button', { name: '好，开始选语种' }).click()
  await page.getByRole('button', { name: '下一步：怎么开始' }).click()
  await page.getByRole('button', { name: '开始定级测试' }).click()
  await page.waitForURL('**/placement?lang=en')
}

/** 逐题选第一项完成 10 道定级题（结果级别不影响主链路，只要完成 enrollment）。 */
export async function finishPlacement(page: Page) {
  for (let i = 0; i < 10; i++) {
    await page.locator('button.option-card').first().click()
  }
  await page.waitForURL('**/placement-result')
}
