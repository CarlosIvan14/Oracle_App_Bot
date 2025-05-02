//04Solutions.test.jsx
// 4. Marcar una tarea como completada.

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
import { mockInProgressTask, mockUnassignedTasks } from "../mocks";

global.fetch = jest.fn();

const mockUser = { idUser: 1 };
const mockProjectUserId = 5;

// Tareas asignadas al usuario, con estado inicial IN_PROGRESS

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
          json: () => Promise.resolve(mockInProgressTask),
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

  test("cambia el estado de la tarea de IN_PROGRESS a COMPLETED", async () => {
    // Mock del prompt y alert
    window.prompt = jest.fn().mockReturnValue("3.5");
    window.alert = jest.fn();
  
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
  
    // Esperar a que la tarea aparezca y termine la carga inicial
    await screen.findByText("Test Task");
    
    // Simular clic en "Completar" y esperar operaciones asíncronas
    userEvent.click(screen.getByText("Completar"));
  
    // Verificar llamada al PATCH y esperar recarga de datos
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        "/api/tasks/101",
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({
            status: "COMPLETED",
            realHours: 3.5
          })
        })
      );
    });
  
    // Esperar a que termine la recarga de tareas
    await screen.findByText("Test Task"); 
  });
  
});
