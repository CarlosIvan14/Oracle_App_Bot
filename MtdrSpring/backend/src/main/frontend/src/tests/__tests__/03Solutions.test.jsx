// 3. Lista de tareas completadas por Sprint. Probar que la información mínima esté presente en el ticket: Nombre Tarea, Nombre de desarrollador, horas estimadas, horas reales.

/* 
    Involved files: 
        - src/App.jsx
        - src/routes/Reports.jsx
*/
global.ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
};

import React from "react";
import { render, screen, waitFor, within } from "@testing-library/react";
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
          expect(screen.getByText("Sprint 1")).toBeInTheDocument();
        });
        
        // Select filters
        userEvent.selectOptions(screen.getByLabelText("Filtrar por"), "sprint");
        userEvent.selectOptions(screen.getByLabelText("Sprint"), "1");
        userEvent.selectOptions(screen.getByLabelText("Miembro"), "all");
        
        // Generate report and wait for data
        userEvent.click(screen.getByText("Generar Reporte"));
        
        // Wait for table to render
        await waitFor(() => {
          expect(screen.getByRole('table')).toBeInTheDocument();
        });
        
        // Verify table contents
        const table = screen.getByRole('table');
        const rows = within(table).getAllByRole('row');
        const taskRow = rows.find(row =>
          within(row).queryByText(/implement login/i)
        );
        expect(taskRow).toBeTruthy();
        const cells = within(taskRow).getAllByRole('cell');
        expect(cells[0]).toHaveTextContent(/john doe/i); // User Name
        expect(cells[1]).toHaveTextContent(/implement login/i); // Task Name
        expect(cells[2]).toHaveTextContent('5'); // Story Points
        expect(cells[3]).toHaveTextContent(/completed/i); // Status
        expect(cells[4]).toHaveTextContent('6'); // Horas Reales
        expect(cells[5]).toHaveTextContent('8'); // Horas Estimadas
      });         
});