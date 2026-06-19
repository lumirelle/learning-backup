import { expect, test } from '@playwright/test'

test.describe('ATS 主路径冒烟', () => {
  test('首页与岗位市场可访问', async ({ page }) => {
    await page.goto('/home')
    await expect(page.getByRole('heading', { name: /ATS|招聘/i }).first()).toBeVisible({ timeout: 15_000 })

    await page.goto('/jobs')
    await expect(page.getByRole('heading', { name: /岗位市场/i })).toBeVisible({ timeout: 15_000 })
  })

  test('HR 登录后可进入岗位管理', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder(/邮箱|email/i).fill('hr@ats.local')
    await page.getByPlaceholder(/密码|password/i).fill('Admin@123')
    await page.getByRole('button', { name: /登录/i }).click()

    await expect(page.getByText(/HR|招聘/i).first()).toBeVisible({ timeout: 20_000 })
    await page.goto('/hr/jobs')
    await expect(page.getByRole('heading', { name: /岗位管理/i })).toBeVisible({ timeout: 15_000 })
    await expect(page.getByText('团队岗位')).toBeVisible()
  })
})
