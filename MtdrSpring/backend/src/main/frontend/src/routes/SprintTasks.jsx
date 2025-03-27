// src/routes/SprintTasks.js
import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import TaskModal from '../components/TaskModal';

// HARCODE tasks
const TASKS_DATA = {
  1: [
    { id: 1001, title: 'Tarea A', status: 'TODO', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1001, title: 'Tarea A', status: 'TODO', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1001, title: 'Tarea A', status: 'TODO', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1001, title: 'Tarea A', status: 'TODO', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1001, title: 'Tarea A', status: 'TODO', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1002, title: 'Tarea B', status: 'IN_PROGRESS', assignee: 'manager', description: 'Lorem ipsum' },
    { id: 1002, title: 'Tarea B', status: 'IN_PROGRESS', assignee: 'manager', description: 'Lorem ipsum' },
    { id: 1002, title: 'Tarea B', status: 'IN_PROGRESS', assignee: 'manager', description: 'Lorem ipsum' },
    { id: 1002, title: 'Tarea B', status: 'IN_PROGRESS', assignee: 'manager', description: 'Lorem ipsum' },
    { id: 1003, title: 'Tarea C', status: 'DONE', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1003, title: 'Tarea C', status: 'DONE', assignee: 'dev', description: 'Lorem ipsum' },
    { id: 1003, title: 'Tarea C', status: 'DONE', assignee: 'dev', description: 'Lorem ipsum' },
  ],
  // etc. HARCODE para cada sprintId
};

function SprintTasks() {
  const { sprintId } = useParams();
  const [selectedTask, setSelectedTask] = useState(null);

  const tasks = TASKS_DATA[sprintId] || [];

  const todo = tasks.filter(t => t.status === 'TODO');
  const inProgress = tasks.filter(t => t.status === 'IN_PROGRESS');
  const done = tasks.filter(t => t.status === 'DONE');

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4">Tareas del Sprint {sprintId}</h1>
      <div className="grid grid-cols-3 gap-4">
        {/* Columna 1: To Do */}
        <div className="bg-black bg-opacity-20 p-4 rounded-2xl">
          <h2 className="text-xl font-bold mb-2">To Do</h2>
          {todo.map(task => (
            <div
              key={task.id}
              className="bg-black bg-opacity-40 p-4 mb-2 rounded-3xl flex justify-between"
            >
              <p className='text-white'>{task.title}</p>
              <button
                className="mt-1 text-blue-300 underline"
                onClick={() => setSelectedTask(task)}
              >
                Ver más
              </button>
            </div>
          ))}
          {todo.length === 0 && <p className="text-gray-400">No hay tareas</p>}
        </div>

        {/* Columna 2: In Progress */}
        <div className="bg-black bg-opacity-20 p-4 rounded-2xl">
          <h2 className="text-xl font-bold mb-2">In Progress</h2>
          {inProgress.map(task => (
            <div
              key={task.id}
              className="bg-black bg-opacity-40 p-4 mb-2 rounded-3xl  flex justify-between"
            >
              <p className='text-white'>{task.title}</p>
              <button
                className="mt-1 text-blue-300 underline"
                onClick={() => setSelectedTask(task)}
              >
                Ver más
              </button>
            </div>
          ))}
          {inProgress.length === 0 && <p className="text-gray-400">No hay tareas</p>}
        </div>

        {/* Columna 3: Done */}
        <div className="bg-black bg-opacity-20 p-4 rounded-2xl">
          <h2 className="text-xl font-bold mb-2">Done</h2>
          {done.map(task => (
            <div
              key={task.id}
              className="bg-black bg-opacity-40 p-4 mb-2 rounded-3xl flex justify-between"
            >
              <p className='text-white'>{task.title}</p>
              <button
                className="mt-1 text-blue-300 underline"
                onClick={() => setSelectedTask(task)}
              >
                Ver más
              </button>
            </div>
          ))}
          {done.length === 0 && <p className="text-gray-400">No hay tareas</p>}
        </div>
      </div>

      {/* Modal para ver detalles */}
      {selectedTask && (
        <TaskModal
          task={selectedTask}
          onClose={() => setSelectedTask(null)}
        />
      )}
    </div>
  );
}

export default SprintTasks;
