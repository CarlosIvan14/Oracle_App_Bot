// src/__tests__/testServer.js
import { setupServer } from "msw/node";
import { rest } from "msw";
import {
  mockInProgressTask,
  mockProject,
  mockSprints,
  mockTasks,
  mockUnassignedTasks,
} from "./mocks";
// Define all default handlers here
const handlers = [
  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/by-sprint/:sprintId`,
    (req, res, ctx) => {
      return res(ctx.json(mockInProgressTask));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/tasks/unassigned/:sprintId`,
    (req, res, ctx) => {
      return res(ctx.json(mockUnassignedTasks));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/sprints/project/:projectId`,
    (req, res, ctx) => {
      return res(ctx.json(mockSprints));
    },
  ),

  rest.get(`${config.apiBaseUrl}/api/projects/:projectId`, (req, res, ctx) => {
    return res(ctx.json(mockProject));
  }),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/team-sprint/:sprintId/done/count`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks.length));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/user/:userId/sprint/:sprintId/done/count`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks.length));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/team-sprint/:sprintId/done`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/user/:userId/sprint/:sprintId/done`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/team-week/:selectedDate/project/:projectId/done/count`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks.length));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/user/:userId/week/:selectedDate/done/count`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks.length));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/team-week/:selectedDate/project/:projectId/done`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/user/:userId/week/:selectedDate/done`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/team-month/:selectedDate/project/:projectId/done/count`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks.length));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/user/:userId/month/:selectedDate/done/count`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks.length));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/team-month/:selectedDate/project/:projectId/done`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks));
    },
  ),

  rest.get(
    `${config.apiBaseUrl}/api/task-assignees/user/:userId/month/:selectedDate/done`,
    (req, res, ctx) => {
      return res(ctx.json(mockTasks));
    },
  ),
];

// Create server instance
const server = setupServer(...handlers);

export { server, rest };
