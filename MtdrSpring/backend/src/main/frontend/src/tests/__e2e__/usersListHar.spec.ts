import { test, expect } from '@playwright/test';

test.describe('HAR file mocking for app users', () => {

  const harFilePath = './hars/user-list.har';
  const targetUrl = '**/api/project-users/project/**/users';

  test('Record HAR file from real user list API @har', async ({ page }) => {
    // Route and update HAR to capture real API response
    await page.routeFromHAR(harFilePath, {
      url: targetUrl,
      update: true,
    });

    await page.goto('http://localhost:8081/');
    await page.locator('input[type="text"]').fill('AdolfoHS');
    await page.locator('input[type="password"]').fill('adolfo123');
    await page.getByRole('button', { name: 'Ingresar' }).click();

    await page.getByRole('heading', { name: 'Task Management Tool Project' }).click();
    await page.getByRole('link', { name: '👥 Ver Usuarios' }).click();

    // Optionally wait for user list to load
    await page.waitForTimeout(2000);
  });

  test('Mock user list API using HAR file @har', async ({ page }) => {
    // Route from existing HAR file in read-only mode (no update)
    await page.routeFromHAR(harFilePath, {
      url: targetUrl,
    });

    await page.goto('http://localhost:8081/');
    await page.locator('input[type="text"]').fill('AdolfoHS');
    await page.locator('input[type="password"]').fill('adolfo123');
    await page.getByRole('button', { name: 'Ingresar' }).click();

    await page.getByRole('heading', { name: 'Task Management Tool Project' }).click();
    await page.getByRole('link', { name: '👥 Ver Usuarios' }).click();
    
    await expect(page.getByRole('cell', { name: 'AdolfoHS mocked' })).toBeVisible();
  });

});
