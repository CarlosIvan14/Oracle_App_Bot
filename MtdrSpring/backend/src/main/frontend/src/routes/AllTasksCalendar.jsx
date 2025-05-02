import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

export default function AllTasksCalendar() {
  const { sprintId } = useParams();
  const [assignedTasks, setAssignedTasks] = useState([]);
  const [unassignedTasks, setUnassignedTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedTask, setSelectedTask] = useState(null);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        setLoading(true);

        const [assignedResponse, unassignedResponse] = await Promise.all([
          fetch(`http://140.84.170.68/api/task-assignees/by-sprint/${sprintId}`),
          fetch(`http://140.84.170.68/api/tasks/unassigned/${sprintId}`)
        ]);

        if (!assignedResponse.ok || !unassignedResponse.ok) {
          throw new Error("Error al cargar las tareas");
        }

        const assignedData = await assignedResponse.json();
        const unassignedData = await unassignedResponse.json();

        const transformedAssigned = assignedData.map((item) => ({
          ...item.task,
          assignee: item.projectUser.user.name,
          assigneeDetails: item.projectUser.user,
        }));

        setAssignedTasks(transformedAssigned);
        setUnassignedTasks(unassignedData);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };

    fetchTasks();
  }, [sprintId]);

  if (loading)
    return <p className="text-center mt-8 text-white">Cargando tareas…</p>;
  if (error) return <p className="text-center mt-8 text-red-500">{error}</p>;

  const renderTaskCard = (task) => (
    <div
      key={task.id}
      className="bg-black bg-opacity-20 p-4 rounded-2xl flex flex-col space-y-2"
    >
      <h3 className="font-semibold text-white truncate">{task.name}</h3>
      <p className="text-gray-300 truncate">
        Desarrollador: {task.assignee || "Libre"}
      </p>
      <p className="text-gray-400 text-sm">
        Story Points:{" "}
        <span
          className={`inline-block px-2 py-0.5 rounded-full text-white ${
            task.storyPoints <= 3
              ? "bg-green-500"
              : task.storyPoints <= 6
                ? "bg-yellow-500"
                : "bg-red-500"
          }`}
        >
          {task.storyPoints}
        </span>
      </p>
      <p className="text-gray-400 text-sm">Estimadas: {task.estimatedHours}h</p>
      <p className="text-gray-400 text-sm">Reales: {task.realHours || 0}h</p>
      <button
        className="mt-auto self-end text-blue-300 underline"
        onClick={() => setSelectedTask(task)}
      >
        Ver más
      </button>
    </div>
  );

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4 text-white">
        Todas las tareas del Sprint {sprintId}
      </h1>

      {/* ==== CONTENEDOR ÚNICO ==== */}
      <h2 className="text-xl font-semibold mb-2 text-white">Tareas</h2>
      <div
        className="no-white-scrollbar overflow-y-auto max-h-[calc(100vh-220px)] pr-2"
        style={{
          scrollbarWidth: "thin",
          scrollbarColor: "rgba(107,114,128,0.6) transparent",
        }}
      >
        {/* Asignadas primero */}
        {assignedTasks.length > 0 && (
          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-2 text-white">
              Tareas Asignadas
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {assignedTasks.map(renderTaskCard)}
            </div>
          </div>
        )}

        {/* Libres al final */}
        {unassignedTasks.length > 0 && (
          <div className="mt-6">
            <h3 className="text-lg font-semibold mb-2 text-white">
              Tareas Libres
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {unassignedTasks.map(renderTaskCard)}
            </div>
          </div>
        )}
      </div>

      {/* ==== MODAL ==== */}
      {selectedTask && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-70"
          onClick={() => setSelectedTask(null)}
        >
          <div
            className="max-w-md w-full p-6 bg-white dark:bg-customDark dark:text-white rounded-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="text-2xl font-bold mb-4">{selectedTask.name}</h2>
            <p className="mb-2">
              <span className="font-semibold">Status:</span>{" "}
              {selectedTask.status}
            </p>
            <p className="mb-2">
              <span className="font-semibold">Desarrollador:</span>{" "}
              {selectedTask.assignee}
            </p>

            {selectedTask.assigneeDetails && (
              <>
                <p className="mb-2">
                  <span className="font-semibold">Email:</span>{" "}
                  {selectedTask.assigneeDetails.email}
                </p>
                <p className="mb-2">
                  <span className="font-semibold">Rol:</span>{" "}
                  {selectedTask.assigneeDetails.role}
                </p>
              </>
            )}

            <p className="mb-2">
              <span className="font-semibold">Story Points:</span>{" "}
              <span
                className={`inline-block px-2 py-0.5 rounded-full text-white ${
                  selectedTask.storyPoints <= 3
                    ? "bg-green-500"
                    : selectedTask.storyPoints <= 6
                      ? "bg-yellow-500"
                      : "bg-red-500"
                }`}
              >
                {selectedTask.storyPoints}
              </span>
            </p>
            <p className="mb-2">
              <span className="font-semibold">Horas Estimadas:</span>{" "}
              {selectedTask.estimatedHours}h
            </p>
            <p className="mb-2">
              <span className="font-semibold">Horas Reales:</span>{" "}
              {selectedTask.realHours || 0}h
            </p>
            <p className="mb-4">
              <span className="font-semibold">Deadline:</span>{" "}
              {new Date(selectedTask.deadline).toLocaleString()}
            </p>
            {selectedTask.description && (
              <p className="mb-4">
                <span className="font-semibold">Descripción:</span>{" "}
                {selectedTask.description}
              </p>
            )}
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
