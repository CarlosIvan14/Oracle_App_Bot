export const mockSprints = [
  { id_sprint: 1, name: "Sprint 1" },
  { id_sprint: 2, name: "Sprint 2" }
];

export const mockProject = {
  creationTs: "2025-04-01T12:00:00.000",
  deletedTs: null,
  description: "Proyecto demo para gestión de equipos y tareas con bot de Telegram y dashboard React/Java en la nube.",
  name: "Demo Task Management Tool",
  projectUsers: [
    {
      idProjectUser: 101,
      user: {
        idUser: 201,
        name: "Alex Lozoya",
        email: "alex.lozoya@example.com",
        status: "Active",
        telegramId: 1234567890,
        phoneNumber: "5551234567",
        password: "mocked_password_1"
      },
      roleUser: "manager",
      status: "active"
    },
    {
      idProjectUser: 102,
      user: {
        idUser: 202,
        name: "Juan Pérez Torres",
        email: "juan.perez@example.com",
        status: "Active",
        telegramId: 2345678901,
        phoneNumber: "5552345678",
        password: "mocked_password_2"
      },
      roleUser: "developer",
      status: "active"
    },
    {
      idProjectUser: 103,
      user: {
        idUser: 203,
        name: "Ana Martínez Ruiz",
        email: "ana.martinez@example.com",
        status: "Active",
        telegramId: null,
        phoneNumber: "5553456789",
        password: "mocked_password_3"
      },
      roleUser: "developer",
      status: "active"
    },
    {
      idProjectUser: 104,
      user: {
        idUser: 204,
        name: "Luis Hernández Soto",
        email: "luis.hernandez@example.com",
        status: "Active",
        telegramId: 3456789012,
        phoneNumber: "5554567890",
        password: "mocked_password_4"
      },
      roleUser: "developer",
      status: "active"
    },
    {
      idProjectUser: 105,
      user: {
        idUser: 205,
        name: "Sofía Ramírez Díaz",
        email: "sofia.ramirez@example.com",
        status: "Active",
        telegramId: null,
        phoneNumber: "5555678901",
        password: "mocked_password_5"
      },
      roleUser: "developer",
      status: "active"
    },
    {
      idProjectUser: 106,
      user: {
        idUser: 206,
        name: "Carlos Gómez Vega",
        email: "carlos.gomez@example.com",
        status: "Active",
        telegramId: null,
        phoneNumber: "5556789012",
        password: "mocked_password_6"
      },
      roleUser: "developer",
      status: "active"
    }
  ],
  id_project: 501
};


// TAREAS CON TASK Y PROJECT USER

// Tareas no asignadas
export const mockUnassignedTasks = [
  {
    id: 2,
    name: "Fix logout bug",
    storyPoints: 2,
    estimatedHours: 3,
    realHours: 0,
    status: "To Do",
    deadline: "2025-05-01T15:00:00Z",
  },
];

// Tareas asignadas al usuario, con estado inicial ASSIGNED
export const mockAssignedTasks = [
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

// Tareas asignadas al usuario, con estado inicial ASSIGNED
export const mockInProgressTask = [
  {
    task: {
      id: 101,
      name: "Test Task",
      description: "Some description",
      storyPoints: 3,
      estimatedHours: 4,
      realHours: null,
      status: "IN_PROGRESS",
    },
    projectUser: {
      user: {
        name: "Test User",
      },
    },
  },
];

export const mockTasks = [
  {
    task: {
      id: 1,
      name: "Implement Login",
      storyPoints: 5,
      status: "Completed",
      estimatedHours: 8,
      realHours: 6,
      sprint: { id: 1, name: "Sprint 1" } // Will be stripped in processing
    },
    projectUser: { user: { name: "Alex Lozoya" } }
  },
];
