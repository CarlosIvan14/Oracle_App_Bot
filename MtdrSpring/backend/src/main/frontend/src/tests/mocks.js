export const assignedTasksMock = [
    {
      task: {
        id: 1,
        name: "Implement login",
        storyPoints: 5,
        estimatedHours: 8,
        realHours: 6,
        status: "In Progress",
        deadline: "2025-04-30T10:00:00Z",
      },
      projectUser: {
        user: {
          name: "Alice",
          email: "alice@example.com",
          role: "Developer",
        },
      },
    },
];
  
export const unassignedTasksMock = [
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