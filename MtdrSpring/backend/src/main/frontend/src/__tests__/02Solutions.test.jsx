// 2. El cambio de estado de algún dato de las tareas (status).

/* 
    Involved files: 
        - src/App.jsx
        - src/routes/SprintTasks.jsx
*/

// tests/SprintTasks.test.jsx

import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import "@testing-library/jest-dom";
import SprintTasks from "../routes/SprintTasks";

global.fetch = jest.fn();

const mockUser = { idUser: 1 };
const mockProjectUserId = 5;

const mockAssignedTasks = [
  {
    task: {
      id: 101,
      name: "Test Task",
      description: "Some description",
      storyPoints: 3,
      estimatedHours: 4,
      realHours: null,
      status: "ASSIGNED",
    },
    projectUser: {
      user: {
        name: "Test User",
      },
    },
  },
];

const mockUnassignedTasks = [];

describe("AllTasksCalendar Component", () => {
  beforeAll(() => {
    jest.spyOn(console, 'warn').mockImplementation((msg) => {
      if (
        msg.includes('React Router Future Flag Warning')
      ) return;
      // Si no es el warning específico, lo imprimimos
      console.warn(msg);
    });
  });  
  beforeEach(() => {
    localStorage.setItem("user", JSON.stringify(mockUser));
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
  
  test("changes task status from ASSIGNED to IN_PROGRESS", async () => {
    render(
      <MemoryRouter initialEntries={["/project/1/sprint/10"]}>
        <Routes>
          <Route path="/project/:projectId/sprint/:sprintId" element={<SprintTasks />} />
        </Routes>
      </MemoryRouter>
    );
  
    expect(await screen.findByText("Test Task")).toBeInTheDocument();
  
    const startButton = screen.getByText("Start");
    fireEvent.click(startButton);
  
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith("/api/tasks/101", expect.objectContaining({
        method: "PATCH",
        body: JSON.stringify({ status: "IN_PROGRESS" }),
      }));
    });
  });  
});
