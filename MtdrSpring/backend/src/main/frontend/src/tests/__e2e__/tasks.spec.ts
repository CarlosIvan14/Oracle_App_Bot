// @ts-check
import { expect, test } from '@playwright/test';

test.describe('Different ways to manage task creation', () => {
    test('free task @tasks', async ({ page }) => {
        await page.goto('http://localhost:8081/');
        await page.locator('input[type="text"]').click();
        await page.locator('input[type="text"]').fill('AdolfoHS');
        await page.locator('input[type="password"]').click();
        await page.locator('input[type="password"]').fill('adolfo123');
        await page.getByRole('button', { name: 'Ingresar' }).click();
        await expect(page.getByRole('heading', { name: 'Tus Proyectos' })).toBeVisible();
        
        await page.getByRole('heading', { name: 'Task Management Tool Project' }).click();
        await expect(page.getByRole('heading', { name: 'teste2e' })).toBeVisible();
        await page.getByRole('heading', { name: 'teste2e' }).click();
        await expect(page.getByRole('button', { name: 'Añadir Tarea' })).toBeVisible();
        await page.getByRole('button', { name: 'Añadir Tarea' }).click({ force: true });
        await expect(page.getByRole('button', { name: 'Free Task' })).toBeVisible();
        await page.getByRole('button', { name: 'Free Task' }).click();

        await page.getByRole('textbox', { name: 'Nombre / título' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).fill('Free Task e2e playwright test');
        await page.getByRole('textbox', { name: 'Descripción' }).click();
        await page.getByRole('textbox', { name: 'Descripción' }).fill('Generate a playwright e2e test for free task flow');
        await page.getByRole('spinbutton', { name: 'Story Points' }).click();
        await page.getByRole('spinbutton', { name: 'Story Points' }).press('ArrowUp');
        await page.getByRole('spinbutton', { name: 'Horas estimadas' }).click();
        await page.getByRole('spinbutton', { name: 'Horas estimadas' }).press('ArrowUp');
        await page.getByRole('textbox', { name: 'Deadline' }).click();
        await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
        await page.getByRole('button', { name: 'Crear Tarea' }).click();
        await expect(page.getByRole('heading', { name: 'Free Task e2e playwright test' })).toBeVisible();
    });
    test('assigned task', async ({ page }) => {
        await page.goto('http://localhost:8081/');
        await page.locator('input[type="text"]').click();
        await page.locator('input[type="text"]').fill('AdolfoHS');
        await page.locator('input[type="password"]').click();
        await page.locator('input[type="password"]').fill('adolfo123');
        await page.getByRole('button', { name: 'Ingresar' }).click();
        await expect(page.getByRole('heading', { name: 'Tus Proyectos' })).toBeVisible();
        
        await page.getByRole('heading', { name: 'Task Management Tool Project' }).click();
        await expect(page.getByRole('heading', { name: 'teste2e' })).toBeVisible();
        await page.getByRole('heading', { name: 'teste2e' }).click();
        await expect(page.getByRole('button', { name: 'Añadir Tarea' })).toBeVisible();
        await page.getByRole('button', { name: 'Añadir Tarea' }).click({ force: true });
        
        await expect(page.getByRole('button', { name: 'Asignar usuario' })).toBeVisible();
        await page.getByRole('button', { name: 'Asignar usuario' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).fill('Assign user test');
        await page.getByRole('textbox', { name: 'Descripción' }).click();
        await page.getByRole('textbox', { name: 'Descripción' }).fill('Use playwright to test e2e assigned task implementation');
        
        await page.getByRole('textbox', { name: 'Deadline' }).click();
        await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
        await page.getByRole('combobox').selectOption('23');
        await page.getByRole('button', { name: 'Crear Tarea' }).click();
        await expect(page.getByRole('heading', { name: 'My Assigned Tasks' })).toBeVisible();
        await page.getByRole('heading', { name: 'Assign user test' }).click();
        await page.getByText('Asignado a: AdolfoHS').click();
    });
    // test('AI suggestion task', async ({ page }) => {
    //     await page.goto('http://localhost:8081/');
    //     await page.locator('input[type="text"]').click();
    //     await page.locator('input[type="text"]').fill('AdolfoHS');
    //     await page.locator('input[type="password"]').click();
    //     await page.locator('input[type="password"]').fill('adolfo123');
    //     await page.getByRole('button', { name: 'Ingresar' }).click();
    //     await expect(page.getByRole('heading', { name: 'Tus Proyectos' })).toBeVisible();
        
    //     await page.getByRole('heading', { name: 'Task Management Tool Project' }).click();
    //     await expect(page.getByRole('heading', { name: 'teste2e' })).toBeVisible();
    //     await page.getByRole('heading', { name: 'teste2e' }).click();
    //     await expect(page.getByRole('button', { name: 'Añadir Tarea' })).toBeVisible();
    //     await page.getByRole('button', { name: 'Añadir Tarea' }).click({ force: true });

    //     await expect(page.getByRole('button', { name: 'Recomendación IA' })).toBeVisible();
    //     await page.getByRole('button', { name: 'Recomendación IA' }).click();
    //     await page.getByRole('textbox', { name: 'Nombre / título' }).click();
    //     await page.getByRole('textbox', { name: 'Nombre / título' }).fill('AI task creation e2e test');
    //     await page.getByRole('textbox', { name: 'Descripción' }).click();
    //     await page.getByRole('textbox', { name: 'Descripción' }).fill('Use playwright to test the AI recommendation feature for task creation');
    //     await page.getByRole('textbox', { name: 'Deadline' }).click();
    //     await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
    //     await page.getByRole('button', { name: 'Obtener recomendación IA' }).click();
    //     await page.getByRole('button', { name: 'Obtener recomendación IA' }).click();
    // });
})