// @ts-check
import { expect, test } from '@playwright/test';


test.describe('Different ways to manage task creation', () => {
    test('free task @real', async ({ page }) => {
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
        await page.getByRole('textbox', { name: 'Nombre / título' }).fill('Free Task V6');
        await page.getByRole('textbox', { name: 'Descripción' }).click();
        await page.getByRole('textbox', { name: 'Descripción' }).fill('Generate a playwright for free task flow 3');
        await page.getByRole('spinbutton', { name: 'Story Points' }).click();
        await page.getByRole('spinbutton', { name: 'Story Points' }).press('ArrowUp');
        await page.getByRole('spinbutton', { name: 'Horas estimadas' }).click();
        await page.getByRole('spinbutton', { name: 'Horas estimadas' }).press('ArrowUp');
        await page.getByRole('textbox', { name: 'Deadline' }).click();
        await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
        await page.getByRole('button', { name: 'Crear Tarea' }).click();
        await expect(page.getByRole('heading', { name: 'Free Task V6', exact: true }))
        .toBeVisible({ timeout: 10000 });
    });
    test('assigned task @real', async ({ page }) => {
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
        await page.getByRole('textbox', { name: 'Nombre / título' }).fill('Assign user v6');
        await page.getByRole('textbox', { name: 'Descripción' }).click();
        await page.getByRole('textbox', { name: 'Descripción' }).fill('Use playwright to test e2e assigned task implementation 3');
        
        await page.getByRole('textbox', { name: 'Deadline' }).click();
        await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
        await page.getByRole('combobox').selectOption('23');
        await page.getByRole('button', { name: 'Crear Tarea' }).click();
        
        await expect(page.getByText('Assign user v6Asignado a:').first()).toBeVisible({ timeout: 20000 });
    });
    test('AI suggestion task mocking response @mock-api', async ({ page }) => {
        await page.route('**/assignment/by-ai', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(mockUsers)  
            });
        });

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

        await expect(page.getByRole('button', { name: 'Recomendación IA' })).toBeVisible();
        await page.getByRole('button', { name: 'Recomendación IA' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).fill('AI task creation v7');
        await page.getByRole('textbox', { name: 'Descripción' }).click();
        await page.getByRole('textbox', { name: 'Descripción' }).fill('Use playwright to test the AI recommendation feature for task creation 3');
        await page.getByRole('textbox', { name: 'Deadline' }).click();
        await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
        await page.getByRole('button', { name: 'Obtener recomendación IA' }).click();
        await expect(page.getByRole('combobox')).toBeVisible();
        await page.getByRole('combobox').selectOption('23');
        await page.getByRole('button', { name: 'Crear Tarea' }).click();
        await expect(page.getByRole('heading', { name: 'AI task creation v7', exact: true }))
        .toBeVisible({ timeout: 20000 });
    });
    test('AI suggestion task with modified API response @modified-api', async ({ page }) => {
        await page.route('**/assignment/by-ai', async (route) => {
            if (route.request().method() === 'POST') {
                const response = await route.fetch();
                const json = await response.json();
                
                json.push({
                    "idUser": 99,
                    "name": "YourName",
                    "email": "your.email@tec.mx",
                    "status": "Active",
                    "telegramId": 1234567890,
                    "phoneNumber": "1234567890"
                });

                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    body: JSON.stringify(json)
                });
            } else {
                await route.continue();
            }
        });

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
        await expect(page.getByRole('button', { name: 'Añadir Tarea' })).toBeVisible({timeout: 20000});
        await page.getByRole('button', { name: 'Añadir Tarea' }).click({ force: true });

        await expect(page.getByRole('button', { name: 'Recomendación IA' })).toBeVisible();
        await page.getByRole('button', { name: 'Recomendación IA' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).click();
        await page.getByRole('textbox', { name: 'Nombre / título' }).fill('AI task mocked');
        await page.getByRole('textbox', { name: 'Descripción' }).click();
        await page.getByRole('textbox', { name: 'Descripción' }).fill('Use playwright to test the AI recommendation feature for task creation 2');
        await page.getByRole('textbox', { name: 'Deadline' }).click();
        await page.getByRole('option', { name: 'Choose Thursday, June 12th,' }).click();
        await page.getByRole('button', { name: 'Obtener recomendación IA' }).click();
        await expect(page.getByRole('combobox')).toBeVisible({timeout: 20000});
        await expect(page.getByRole('combobox')).toContainText('YourName');
    });
})

const mockUsers = [
    {
        "idUser": 23,
        "name": "AdolfoHS",
        "email": "A01637184@tec.mx",
        "status": "Active",
        "telegramId": 7878998173,
        "phoneNumber": "3322543331"
    },
    {
        "idUser": 24,
        "name": "ArturoRM22",
        "email": "A01643269@tec.mx",
        "status": "Active",
        "telegramId": 7878998173,
        "phoneNumber": "4381184550"
    },
    {
        "idUser": 25,
        "name": "Moisés Adrián Cortés Ramos",
        "email": "A01642492@tec.mx",
        "status": "Active",
        "telegramId": null,
        "phoneNumber": "3751269010"
    },
    {
        "idUser": 22,
        "name": "JJ",
        "email": "A01637706@tec.mx",
        "status": "Active",
        "telegramId": null,
        "phoneNumber": "3334546700"
    },
    {
        "idUser": 21,
        "name": "CarlosIv14",
        "email": "A01643070@tec.mx",
        "status": "Active",
        "telegramId": 5274229047,
        "phoneNumber": "4381336297"
    },
    {
        "idUser": 26,
        "name": "Bryan Ithan Landín Lara",
        "email": "A01636271@tec.mx",
        "status": "Active",
        "telegramId": null,
        "phoneNumber": "6122013067"
    }
];