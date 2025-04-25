// 1. Visualización en tiempo real de tareas asignadas a cada usuario.

/* 
    Involved files: 
        - src/App.jsx
        - src/routes/AllTasksCalendar.jsx
*/

// src/__tests__/01AllTasksCalendar.test.jsx
import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import "@testing-library/jest-dom";

import AllTasksCalendar from "../../routes/AllTasksCalendar";
import { server } from "../testServer";

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

const renderWithRouter = (ui, { route = "/calendar/123" } = {}) => {
  window.history.pushState({}, "Test page", route);
  return render(
    <MemoryRouter initialEntries={[route]}>
      <Routes>
        <Route path="/calendar/:sprintId" element={ui} />
      </Routes>
    </MemoryRouter>,
  );
};

describe("AllTasksCalendar Component", () => {
  beforeAll(() => {
    jest.spyOn(console, 'warn').mockImplementation((msg) => {
      if (
        msg.includes('React Router Future Flag Warning')
      ) return;
      console.warn(msg);
    });
  });

  test("renders assigned and unassigned tasks correctly", async () => {
    renderWithRouter(<AllTasksCalendar />);

    expect(screen.getByText(/cargando tareas/i)).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.queryByText(/cargando tareas/i)).not.toBeInTheDocument(),
    );

    expect(screen.getByText("Implement login")).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador: Alice/i)).toBeInTheDocument();

    expect(screen.getByText("Fix logout bug")).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador: Libre/i)).toBeInTheDocument();
  });
});