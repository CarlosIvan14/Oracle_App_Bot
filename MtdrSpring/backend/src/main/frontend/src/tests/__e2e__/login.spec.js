// @ts-check
import { test, expect } from '@playwright/test';
import config from "../../config";

test.describe('Login Accessible', () => {
    test('login form exists', async ({ page }) => {
      await page.goto(`${config.apiBaseUrl}`);
      await expect(page).toHaveTitle(/Todo application/);
      let name = page.locator('input[type="text"]');
      let password = page.locator('input[type="password"]');
      let button = page.getByRole('button', { name: 'Ingresar' })
      await name.fill('fakeadmin');
      await password.fill('fakepassword');
      await button.click();
      // add error message.
    });
})