# Test info

- Name: New Todo >> login form exists and gives error message with invalid credentials
- Location: /home/sign0ret/dev/student/s6/oracle/Oracle_App_Bot/MtdrSpring/backend/src/main/frontend/src/tests/__e2e__/login.spec.js:6:9

# Error details

```
Error: browserType.launch: Target page, context or browser has been closed
Browser logs:

<launching> /home/sign0ret/.cache/ms-playwright/webkit-2158/pw_run.sh --inspector-pipe --headless --no-startup-window
<launched> pid=2639178
[pid=2639178][err] /home/sign0ret/.cache/ms-playwright/webkit-2158/minibrowser-wpe/bin/MiniBrowser: error while loading shared libraries: libicudata.so.70: cannot open shared object file: No such file or directory
Call log:
  - <launching> /home/sign0ret/.cache/ms-playwright/webkit-2158/pw_run.sh --inspector-pipe --headless --no-startup-window
  - <launched> pid=2639178
  - [pid=2639178][err] /home/sign0ret/.cache/ms-playwright/webkit-2158/minibrowser-wpe/bin/MiniBrowser: error while loading shared libraries: libicudata.so.70: cannot open shared object file: No such file or directory

```

# Test source

```ts
   1 | // @ts-check
   2 | import { test, expect } from '@playwright/test';
   3 | import config from "../../config";
   4 |
   5 | test.describe('New Todo', () => {
>  6 |     test('login form exists and gives error message with invalid credentials', async ({ page }) => {
     |         ^ Error: browserType.launch: Target page, context or browser has been closed
   7 |       await page.goto(`${config.apiBaseUrl}`);
   8 |       await expect(page).toHaveTitle(/Todo application/);
   9 |       let name = page.locator('input[type="text"]');
  10 |       let password = page.locator('input[type="password"]');
  11 |       let button = page.getByRole('button', { name: 'Ingresar' })
  12 |       await name.fill('fakeadmin');
  13 |       await password.fill('fakepassword');
  14 |       await button.click();
  15 |       // add error message.
  16 |     });
  17 | })
  18 |
```