import { test, expect } from '@playwright/test'
import { registerViaUi, startPlacement, finishPlacement, uniqueUser } from './utils'

test.describe('B · 学习主链路（Task 11/12 真实接口）', () => {
  test('注册 → 定级 → 课程地图 → 四环节 → 盖章结算 → 地图出现已完成站点', async ({ page }) => {
    const { username } = uniqueUser('ln')
    test.info().annotations.push({ type: 'account', description: username })

    // 1. 注册 + 引导 + 定级
    await registerViaUi(page, username)
    await startPlacement(page)
    await finishPlacement(page)

    // 2. 定级结果 → 课程地图：至少有一个可解锁课时站点
    await page.getByRole('link', { name: '查看课程地图' }).click()
    await page.waitForURL('**/course-map?lang=en')
    await expect(page.locator('a.stop').first()).toBeVisible()

    // 3. 进入第一节解锁课时
    await page.locator('a.stop').first().click()
    await page.waitForURL(/\/lesson\?id=\d+/)

    // 4. VOCAB：8 张闪卡全部点「认识」（exact 避免命中「不认识」），最后一张自动上报环节成绩
    for (let i = 0; i < 8; i++) {
      await page.getByRole('button', { name: '认识', exact: true }).click()
    }
    await expect(page.getByText('单词环节已完成')).toBeVisible()

    // 5. GRAMMAR：每题选第一项后提交（课时详情异步加载，先等题目渲染）
    await page.getByRole('link').filter({ hasText: '语法练习' }).click()
    await page.waitForURL(/\/grammar\?id=\d+/)
    const questions = page.locator('section.card')
    await expect(questions.first()).toBeVisible()
    const qCount = await questions.count()
    for (let i = 0; i < qCount; i++) {
      await questions.nth(i).locator('button').first().click()
    }
    await page.getByRole('button', { name: /提交答案/ }).click()
    await expect(page.getByRole('link', { name: /返回课时/ })).toBeVisible()
    await page.getByRole('link', { name: /返回课时/ }).click()

    // 6. LISTENING：选第一个 A 选项提交（避开同页的「播放对话」按钮）
    await page.getByRole('link').filter({ hasText: '听力训练' }).click()
    await page.waitForURL(/\/listening\?id=\d+/)
    const optionA = page.getByRole('button', { name: /^A\S/ })
    await expect(optionA.first()).toBeVisible()
    await optionA.first().click()
    await page.getByRole('button', { name: '提交答案' }).click()
    await page.getByRole('link', { name: /返回课时/ }).click()

    // 7. SPEAKING：headless 无语音识别，逐句自评「流利」后提交
    await page.getByRole('link').filter({ hasText: '口语跟读' }).click()
    await page.waitForURL(/\/speaking\?id=\d+/)
    const fluentBtns = page.getByRole('button', { name: '流利' })
    await expect(fluentBtns.first()).toBeVisible()
    const sCount = await fluentBtns.count()
    for (let i = 0; i < sCount; i++) {
      await fluentBtns.nth(i).click()
    }
    await page.getByRole('button', { name: '提交跟读评分' }).click()
    await page.getByRole('link', { name: '返回课时' }).click()

    // 8. 四环节齐 → 出现盖章入口
    await expect(page.getByRole('link', { name: /完成课时 · 盖章/ })).toBeVisible()
    await page.getByRole('link', { name: /完成课时 · 盖章/ }).click()
    await page.waitForURL(/\/lesson-complete\?id=\d+/)

    // 9. 盖章结算：XP/连续学习/首课成就
    await page.getByRole('button', { name: /盖章结算/ }).click()
    await expect(page.getByText('STAMPED')).toBeVisible()
    await expect(page.getByText(/\+\d+\s*XP/)).toBeVisible()
    await expect(page.getByText('初学乍练')).toBeVisible()

    // 10. 回到课程地图：本节课站点已打勾（completed 态）
    await page.goto('/course-map?lang=en')
    await expect(page.locator('span.dot.done')).toHaveCount(1)
  })
})
