import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

export default function AllTasksCalendar() {
  const { sprintId } = useParams();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedTask, setSelectedTask] = useState(null);

  useEffect(() => {
    fetch(`http://localhost:8081/api/tasks/sprint/${sprintId}`)
      .then(res => {
        if (!res.ok) throw new Error('Error al cargar todas las tareas');
        return res.json();
      })
      .then(data => {
        // Transform the data to match the expected format
        const transformedTasks = data.map(task => ({
          ...task,
          // Get the first assignee's name or set to 'Libre'
          assignee: task.assignees?.length > 0 
            ? task.assignees[0].projectUser.user.name 
            : 'Libre',
          // Flatten the assignee details for the modal
          assigneeDetails: task.assignees?.length > 0
            ? {
                name: task.assignees[0].projectUser.user.name,
                email: task.assignees[0].projectUser.user.email,
                role: task.assignees[0].projectUser.roleUser
              }
            : null
        }));
        setTasks(transformedTasks);
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [sprintId]);

  if (loading) return <p className="text-center mt-8 text-white">Cargando tareas…</p>;
  if (error)   return <p className="text-center mt-8 text-red-500">{error}</p>;

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4 text-white">Todas las tareas del Sprint {sprintId}</h1>

      {/* Scroll container styles */}
      <style>{`
        .no-white-scrollbar::-webkit-scrollbar { width: 8px; }
        .no-white-scrollbar::-webkit-scrollbar-track { background: transparent; }
        .no-white-scrollbar::-webkit-scrollbar-thumb { background-color: rgba(107,114,128,0.6); border-radius: 4px; }
      `}</style>

      {/* Scroll container */}
      <div
        className="no-white-scrollbar overflow-y-auto max-h-[calc(100vh-200px)] pr-2"
        style={{
          scrollbarWidth: 'thin',
          scrollbarColor: 'rgba(107,114,128,0.6) transparent',
        }}
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {tasks.map(task => (
            <div
              key={task.id}
              className="bg-black bg-opacity-20 p-4 rounded-2xl flex flex-col space-y-2"
            >
              <h3 className="font-semibold text-white truncate">{task.name}</h3>
              <p className="text-gray-300 truncate">
                Desarrollador: {task.assignee}
              </p>
              <p className="text-gray-400 text-sm">
                Story Points:{' '}
                <span
                  className={`inline-block px-2 py-0.5 rounded-full text-white ${
                    task.storyPoints <= 3 ? 'bg-green-500' :
                    task.storyPoints <= 6 ? 'bg-yellow-500' : 'bg-red-500'
                  }`}
                >
                  {task.storyPoints}
                </span>
              </p>
              <p className="text-gray-400 text-sm">
                Estimadas: {task.estimatedHours}h
              </p>
              <p className="text-gray-400 text-sm">
                Reales: {task.realHours || 0}h
              </p>
              <button
                className="mt-auto self-end text-blue-300 underline"
                onClick={() => setSelectedTask(task)}
              >
                Ver más
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Modal de detalles */}
      {selectedTask && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-70"
          onClick={() => setSelectedTask(null)}
        >
          <div
            className="max-w-md w-full p-6 bg-white dark:bg-customDark dark:text-white rounded-2xl"
            onClick={e => e.stopPropagation()}
          >
            <h2 className="text-2xl font-bold mb-4">{selectedTask.name}</h2>
            <p className="mb-2">
              <span className="font-semibold">Status:</span>{' '}
              {selectedTask.status}
            </p>
            <p className="mb-2">
              <span className="font-semibold">Desarrollador:</span>{' '}
              {selectedTask.assignee}
            </p>
            
            {selectedTask.assigneeDetails && (
              <>
                <p className="mb-2">
                  <span className="font-semibold">Email:</span>{' '}
                  {selectedTask.assigneeDetails.email}
                </p>
                <p className="mb-2">
                  <span className="font-semibold">Rol:</span>{' '}
                  {selectedTask.assigneeDetails.role}
                </p>
              </>
            )}
            
            <p className="mb-2">
              <span className="font-semibold">Story Points:</span>{' '}
              <span
                className={`inline-block px-2 py-0.5 rounded-full text-white ${
                  selectedTask.storyPoints <= 3 ? 'bg-green-500' :
                  selectedTask.storyPoints <= 6 ? 'bg-yellow-500' : 'bg-red-500'
                }`}
              >
                {selectedTask.storyPoints}
              </span>
            </p>
            <p className="mb-2">
              <span className="font-semibold">Horas Estimadas:</span>{' '}
              {selectedTask.estimatedHours}h
            </p>
            <p className="mb-2">
              <span className="font-semibold">Horas Reales:</span>{' '}
              {selectedTask.realHours || 0}h
            </p>
            <p className="mb-4">
              <span className="font-semibold">Deadline:</span>{' '}
              {new Date(selectedTask.deadline).toLocaleString()}
            </p>
            <p className="mb-4">
              <span className="font-semibold">Descripción:</span>{' '}
              {selectedTask.description}
            </p>
            <button
              onClick={() => setSelectedTask(null)}
              className="mt-4 rounded-full border border-gray-300 px-4 py-2 hover:bg-gray-200 dark:hover:bg-customDarkligth"
            >
              Cerrar
            </button>
          </div>
        </div>
      )}
    </div>
  );
}