//02Solutions.test.jsx
// 2. El cambio de estado de algún dato de las tareas (status).

/*
    Involved files:
        - src/App.jsx
        - src/routes/SprintTasks.jsx
*/

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";

import SprintTasks from "../../routes/SprintTasks";
import { mockAssignedTasks, mockUnassignedTasks } from "../mocks";

global.fetch = jest.fn();

const mockUser = { idUser: 1 };
const mockProjectUserId = 5;



describe("SprintTasks Component", () => {
  beforeAll(() => {
    // Suprime warnings irrelevantes de React Router
    jest.spyOn(console, "warn").mockImplementation((msg) => {
      if (msg.includes("React Router Future Flag Warning")) return;
      console.warn(msg);
    });
  });

  beforeEach(() => {
    // Prepara localStorage con usuario mock
    localStorage.setItem("user", JSON.stringify(mockUser));

    // Mock de todas las llamadas fetch usadas por el componente
    fetch.mockImplementation((url, options) => {
      if (url.includes("/api/project-users/project-id")) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockProjectUserId),
        });
      }
      if (url.includes("/api/task-assignees/user")) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockAssignedTasks),
        });
      }
      if (url.includes("/api/tasks/unassigned")) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockUnassignedTasks),
        });
      }
      if (url.includes("/api/tasks/101") && options?.method === "PATCH") {
        return Promise.resolve({ ok: true });
      }
      return Promise.reject(new Error("Unhandled request"));
    });
  });

  afterEach(() => {
    fetch.mockReset();
    localStorage.clear();
  });

  test("cambia el estado de la tarea de ASSIGNED a IN_PROGRESS", async () => {
    // Renderiza el componente en la ruta correspondiente
    render(
      <MemoryRouter initialEntries={["/project/1/sprint/10"]}>
        <Routes>
          <Route
            path="/project/:projectId/sprint/:sprintId"
            element={<SprintTasks />}
          />
        </Routes>
      </MemoryRouter>
    );

    // Espera que la tarea aparezca en pantalla
    expect(await screen.findByText("Test Task")).toBeInTheDocument();

    // Simula el clic en el botón "Start" para iniciar la tarea
    userEvent.click(screen.getByText("Start"));

    // Verifica que se haya llamado al endpoint PATCH con el nuevo estado
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        "/api/tasks/101",
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({ status: "IN_PROGRESS" }),
        })
      );
    });
  });
});
