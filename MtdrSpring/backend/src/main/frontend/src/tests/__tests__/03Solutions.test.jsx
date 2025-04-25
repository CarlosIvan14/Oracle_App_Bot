// 3. Lista de tareas completadas por Sprint. Probar que la información mínima esté presente en el ticket: Nombre Tarea, Nombre de desarrollador, horas estimadas, horas reales.

/* 
    Involved files: 
        - src/App.jsx
        - src/routes/Reports.jsx

    Flow on Reports.jsx:
        1. click on 
*/

// import React from "react";
// import { render, screen } from "@testing-library/react";
// import { MemoryRouter, Route, Routes } from "react-router-dom";
// import "@testing-library/jest-dom";
// import Reports from "../../routes/Reports";

// const renderWithRouter = (ui, { route = "/projects/123/reports" } = {}) => {
//   return render(
//     <MemoryRouter initialEntries={[route]}>
//       <Routes>
//         <Route path="/projects/:projectId/reports" element={
//           <React.Suspense fallback={<div>Loading...</div>}>
//             {ui}
//           </React.Suspense>
//         } />
//       </Routes>
//     </MemoryRouter>
//   );
// };
// describe("Reports Component", () => {
//   beforeAll(() => {
//     jest.spyOn(console, 'warn').mockImplementation((msg) => {
//         if (
//         msg.includes('React Router Future Flag Warning')
//         ) return;
//         // Si no es el warning específico, lo imprimimos
//         console.warn(msg);
//     });
//   });
//   test("renders the Reports component (loads static UI)", () => {
//     renderWithRouter(<Reports />);
//     expect(screen.getByRole("heading", { name: /reportes/i })).toBeInTheDocument();
//   });
// });

// src/__tests__/03ReportsTasks.test.jsx
import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";

import Reports from "../../routes/Reports";

import { server } from "../testServer";

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

const renderWithRouter = (ui, { route = "/projects/123/reports" } = {}) => {
  window.history.pushState({}, "Test page", route);
  return render(
    <MemoryRouter initialEntries={[route]}>
      <Routes>
        <Route path="/projects/:projectId/reports" element={ui} />
      </Routes>
    </MemoryRouter>,
  );
};

describe("Completed Tasks List in Sprint Reports", () => {
    beforeAll(() => {
        jest.spyOn(console, 'warn').mockImplementation((msg) => {
          if (
            msg.includes('React Router Future Flag Warning')
          ) return;
          // Si no es el warning específico, lo imprimimos
          console.warn(msg);
        });
      });  
      test("displays task details when selecting sprint and team", async () => {
        renderWithRouter(<Reports />);
      
        // Wait for initial data load
        await waitFor(() => {
          expect(screen.getByLabelText("Sprint")).toBeInTheDocument();
        });
        
        // Select filters
        await userEvent.selectOptions(screen.getByLabelText("Filtrar por"), "sprint");
        await userEvent.selectOptions(screen.getByLabelText("Sprint"), "1");
        await userEvent.selectOptions(screen.getByLabelText("Miembro"), "all");
        
        // Generate report and wait for data
        userEvent.click(screen.getByText("Generar Reporte"));
        
        // Wait for table to render
        await waitFor(() => {
          expect(screen.getByText("Tareas Completadas")).toBeInTheDocument();
        });
        
        // Verify table contents
        expect(screen.getByText("Implement Login")).toBeInTheDocument();
        expect(screen.getByText("John Doe")).toBeInTheDocument();
        expect(screen.getByText("8")).toBeInTheDocument(); // Estimated
        expect(screen.getByText("6")).toBeInTheDocument(); // Real
      });         
});