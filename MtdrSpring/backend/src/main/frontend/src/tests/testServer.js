// src/__tests__/testServer.js
import { setupServer } from "msw/node";
import { rest } from "msw";
import { assignedTasksMock, unassignedTasksMock } from "./mocks";

// Define all default handlers here
const handlers = [
  rest.get("http://localhost:8081/api/task-assignees/by-sprint/:sprintId", (req, res, ctx) => {
    return res(ctx.json(assignedTasksMock));
  }),

  rest.get("http://localhost:8081/api/tasks/unassigned/:sprintId", (req, res, ctx) => {
    return res(ctx.json(unassignedTasksMock));
  }),
];

// Create server instance
const server = setupServer(...handlers);

export { server, rest };