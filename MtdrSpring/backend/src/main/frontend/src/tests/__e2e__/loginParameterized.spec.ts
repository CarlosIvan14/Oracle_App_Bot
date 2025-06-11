import { test, expect } from '@playwright/test';

test.describe('Login Tests', () => {

  [
    { name: 'ArturoRM22', password: 'arturo123' },
    { name: 'AdolfoHS', password: 'adolfo123' },
    { name: 'CarlosIv14', password: 'carlos123' }
  ].forEach(({ name, password }) => {
    
    test(`@login Login attempt with: ${name}`, async ({ page }) => {
      await page.goto('http://localhost:8081/');
      await page.locator('input[type="text"]').click();
      await page.locator('input[type="text"]').fill(name);
      await page.locator('input[type="password"]').click();
      await page.locator('input[type="password"]').fill(password);
      await page.getByRole('button', { name: 'Ingresar' }).click();
      await expect(page.getByRole('heading', { name: 'Tus Proyectos' })).toBeVisible();
      await expect(page.getByRole('button', { name: '👤' })).toBeVisible();
      await page.getByRole('button', { name: '👤' }).click();
      await expect(page.getByRole('button', { name: 'Perfil' })).toBeVisible();
      await page.getByRole('button', { name: 'Perfil' }).click();
      await expect(page.getByRole('heading', { name: `${name}` })).toBeVisible();
    });
  });

  [
    { name: 'ArturoRM22', password: 'wrongpass' },
    { name: 'UnknownUser', password: '123456' },
    { name: 'Attacker', password: 'ggs' }
  ].forEach(({ name, password }) => {
    
    test(`@login Login attempt invalid credentials: ${name}`, async ({ page }) => {
      await page.goto('http://localhost:8081/');
      await page.locator('input[type="text"]').click();
      await page.locator('input[type="text"]').fill(name);
      await page.goto('chrome-error://chromewebdata/');
      await page.locator('input[type="password"]').click();
      await page.locator('input[type="password"]').fill(password);
      await page.getByRole('button', { name: 'Ingresar' }).click();

      await expect(page.getByText('Usuario/contraseña inválidos')).toBeVisible();
    });
  });

});
