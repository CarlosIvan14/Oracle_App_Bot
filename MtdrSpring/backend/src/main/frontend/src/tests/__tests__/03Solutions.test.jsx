//03Solutions.test.jsx
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

/**
 * Configura el servidor MSW antes de todos los tests y lo restablece entre cada uno.
 */
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

/**
 * Renderiza un componente dentro de un MemoryRouter,
 * situándose en la ruta indicada para simular navegación.
 *
 * @param ui Componente React a renderizar.
 * @param options.route Ruta inicial (por defecto "/projects/123/reports").
 * @returns {RenderResult} Resultado de renderizado de Testing Library.
 */
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

describe("Lista de tareas completadas en Reports", () => {
  beforeAll(() => {
    // Suprime warnings irrelevantes de React Router
    jest.spyOn(console, "warn").mockImplementation((msg) => {
      if (msg.includes("React Router Future Flag Warning")) return;
      console.warn(msg);
    });
  });

  test("muestra detalles de tarea tras seleccionar sprint y equipo", async () => {
    renderWithRouter(<Reports />);

    // Espera a que cargue la lista de sprints
    await waitFor(() => {
      expect(screen.getByText("Sprint 1")).toBeInTheDocument();
    });

    // Aplica filtros de sprint y miembro
    userEvent.selectOptions(screen.getByLabelText("Filtrar por"), "sprint");
    userEvent.selectOptions(screen.getByLabelText("Sprint"), "1");
    userEvent.selectOptions(screen.getByLabelText("Miembro"), "all");

    // Genera el reporte
    userEvent.click(screen.getByText("Generar Reporte"));

    // Espera a que aparezca la tabla de resultados
    await waitFor(() => {
      expect(screen.getByRole("table")).toBeInTheDocument();
    });

    // Verifica que la fila de la tarea contenga la información clave
    const table = screen.getByRole("table");
    const rows = within(table).getAllByRole("row");
    const taskRow = rows.find((row) =>
      within(row).queryByText(/implement login/i),
    );
    expect(taskRow).toBeTruthy();

    const cells = within(taskRow).getAllByRole("cell");
    expect(cells[0]).toHaveTextContent(/alex lozoya/i); // Nombre de desarrollador
    expect(cells[1]).toHaveTextContent(/implement login/i); // Nombre de tarea
    expect(cells[2]).toHaveTextContent("5"); // Story Points
    expect(cells[3]).toHaveTextContent(/completed/i); // Estado
    expect(cells[4]).toHaveTextContent("6"); // Horas Reales
    expect(cells[5]).toHaveTextContent("8"); // Horas Estimadas
  });
});
