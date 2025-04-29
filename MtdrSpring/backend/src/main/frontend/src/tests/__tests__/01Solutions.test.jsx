//01Solutions.test.jsx
// 1. Visualización en tiempo real de tareas asignadas a cada usuario.

/*
    Involved files:
        - src/App.jsx
        - src/routes/AllTasksCalendar.jsx
*/

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import "@testing-library/jest-dom";

import AllTasksCalendar from "../../routes/AllTasksCalendar";
import { server } from "../testServer";

/**
 * Inicia el servidor mock antes de todos los tests y lo cierra al finalizar.
 */
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

/**
 * Renderiza el componente en un contexto de router,
 * posicionándose en la ruta especificada para simular acceso por sprintId.
 *
 * @param ui Componente React a renderizar.
 * @param options.route Ruta inicial (por defecto "/calendar/123").
 * @returns {RenderResult} Resultado de renderizado de Testing Library.
 */
const renderWithRouter = (ui, { route = "/calendar/123" } = {}) => {
  window.history.pushState({}, "Test page", route);
  return render(
    <MemoryRouter initialEntries={[route]}>
      <Routes>
        <Route path="/calendar/:sprintId" element={ui} />
      </Routes>
    </MemoryRouter>
  );
};

describe("Componente AllTasksCalendar", () => {
  beforeAll(() => {
    // Silencia warnings irrelevantes de React Router
    jest.spyOn(console, "warn").mockImplementation((msg) => {
      if (msg.includes("React Router Future Flag Warning")) return;
      console.warn(msg);
    });
  });

  test("muestra tareas asignadas y sin asignar correctamente y coincide con el snapshot", async () => {
    // Desestructuramos asFragment para snapshot
    const { asFragment } = renderWithRouter(<AllTasksCalendar />);

    // Verifica el estado de carga inicial
    expect(screen.getByText(/cargando tareas/i)).toBeInTheDocument();

    // Espera a que termine la carga
    await waitFor(() =>
      expect(screen.queryByText(/cargando tareas/i)).not.toBeInTheDocument()
    );

    // Captura el snapshot de la UI cargada
    expect(asFragment()).toMatchSnapshot();

    // Confirma tarea con desarrollador asignado
    expect(screen.getByText("Implement login")).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador: Alice/i)).toBeInTheDocument();

    // Confirma tarea sin desarrollador (Libre)
    expect(screen.getByText("Fix logout bug")).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador: Libre/i)).toBeInTheDocument();
  });
});
