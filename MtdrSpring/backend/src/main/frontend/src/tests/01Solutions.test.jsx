// 1. Visualización en tiempo real de tareas asignadas a cada usuario.

/* 
    Involved files: 
        - App.jsx
        - AllTasksCalendar.jsx
        
*/

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { rest } from "msw";
import { setupServer } from "msw/node";
import "@testing-library/jest-dom";
import AllTasksCalendar from "../components/AllTasksCalendar";

// Mock server data
const assignedTasksMock = [
  {
    task: {
      id: 1,
      name: "Implement login",
      storyPoints: 5,
      estimatedHours: 8,
      realHours: 6,
      status: "In Progress",
      deadline: "2025-04-30T10:00:00Z"
    },
    projectUser: {
      user: {
        name: "Alice",
        email: "alice@example.com",
        role: "Developer"
      }
    }
  }
];

const unassignedTasksMock = [
  {
    id: 2,
    name: "Fix logout bug",
    storyPoints: 2,
    estimatedHours: 3,
    realHours: 0,
    status: "To Do",
    deadline: "2025-05-01T15:00:00Z"
  }
];

// Setup MSW handlers
const server = setupServer(
  rest.get("http://localhost:8081/api/task-assignees/by-sprint/:sprintId", (req, res, ctx) => {
    return res(ctx.json(assignedTasksMock));
  }),
  rest.get("http://localhost:8081/api/tasks/unassigned/:sprintId", (req, res, ctx) => {
    return res(ctx.json(unassignedTasksMock));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// Helper for rendering with router
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

describe("AllTasksCalendar Component", () => {
  test("renders assigned and unassigned tasks correctly", async () => {
    renderWithRouter(<AllTasksCalendar />);

    // Loading text should appear first
    expect(screen.getByText(/cargando tareas/i)).toBeInTheDocument();

    // Wait for loading to finish
    await waitFor(() => expect(screen.queryByText(/cargando tareas/i)).not.toBeInTheDocument());

    // Assigned task should show assignee name
    expect(screen.getByText("Implement login")).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador: Alice/i)).toBeInTheDocument();

    // Unassigned task should show as "Libre"
    expect(screen.getByText("Fix logout bug")).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador: Libre/i)).toBeInTheDocument();
  });
});
