// src/routes/SprintTasks.js
import React, { useState, useEffect, useCallback } from "react";
import { useParams } from "react-router-dom";
import TaskModal from "../components/TaskModal";
import NewTaskModal from "../components/NewTaskModal";

export default function SprintTasks() {
  const { projectId, sprintId } = useParams();
  const [projectUserId, setprojectUserId] = useState();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [freeTasks, setFreeTasks] = useState([]);
  const [myAssigned, setMyAssigned] = useState([]);
  const [inProgress, setInProgress] = useState([]);
  const [completed, setCompleted] = useState([]);
  const [selectedTask, setSelectedTask] = useState(null);

  const loadTasks = useCallback(async () => {
    setLoading(true);
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user) throw new Error("Usuario no logueado");

      // 1) Obtener projectUserId
      const puRes = await fetch(
        `/api/project-users/project-id/${projectId}/user-id/${user.idUser}`,
      );
      if (!puRes.ok) throw new Error("Error al obtener projectUserId");
      const projectUserId = await puRes.json();
      setprojectUserId(projectUserId);

      // 2) Traer asignadas y libres
      const [assignedList, unassignedList] = await Promise.all([
        fetch(
          `/api/task-assignees/user/${projectUserId}/sprint/${sprintId}`,
        ).then((r) => (r.ok ? r.json() : Promise.reject())),
        fetch(`/api/tasks/unassigned/${sprintId}`).then((r) =>
          r.ok ? r.json() : Promise.reject(),
        ),
      ]);

      const mapped = assignedList.map((item) => ({
        ...item.task,
        assignee: item.projectUser.user.name,
      }));

      setFreeTasks(unassignedList);
      setMyAssigned(mapped.filter((t) => t.status === "ASSIGNED"));
      setInProgress(mapped.filter((t) => t.status === "IN_PROGRESS"));
      setCompleted(mapped.filter((t) => t.status === "COMPLETED"));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [projectId, sprintId]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const patchTask = async (taskId, payload) => {
    const res = await fetch(`/api/tasks/${taskId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error("Error actualizando tarea");
    await loadTasks();
  };

  const handleTake = async (task) => {
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user) throw new Error("Usuario no logueado");

      // 2. Create the task assignment
      const assignRes = await fetch("/api/task-assignees", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          projectUser: { idProjectUser: projectUserId },
          task: { id: task.id },
        }),
      });

      if (!assignRes.ok) throw new Error("Error asignando la tarea");

      // 3. Update task status
      await patchTask(task.id, { status: "ASSIGNED" });
    } catch (error) {
      setError(error.message);
    }
  };
  const handleStart = (t) => patchTask(t.id, { status: "IN_PROGRESS" });
  const handleUndo = (t) => patchTask(t.id, { status: "IN_PROGRESS" });
  const handleStop = (t) => alert('Función "Parar" no implementada aún');
  const handleComplete = async (t) => {
    const rh = parseFloat(window.prompt("¿Horas reales?", ""));
    if (isNaN(rh)) return alert("Horas inválidas");
    await patchTask(t.id, { status: "COMPLETED", realHours: rh });
  };

  if (loading)
    return <p className="text-center mt-8 text-white">Cargando tareas…</p>;
  if (error) return <p className="text-center mt-8 text-red-500">{error}</p>;

  const renderCard = (task, isFree) => (
    <div key={task.id} className="bg-black bg-opacity-20 p-4 mb-2 rounded-2xl">
      <h3 className="font-semibold text-white truncate">{task.name}</h3>

      {isFree ? (
        <>
          <p className="text-gray-300 truncate">{task.description}</p>
          <p className="text-gray-300">
            Story Points:
            <span
              className={`ml-1 inline-block px-2 py-0.5 rounded-full text-white ${
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
        </>
      ) : (
        <p className="text-gray-300">Asignado a: {task.assignee}</p>
      )}

      <div className="flex justify-between text-sm text-gray-400 mb-2">
        <span>Estimadas: {task.estimatedHours}h</span>
        <span>Reales: {task.realHours}h</span>
      </div>

      <div className="flex justify-between items-center">
        <button
          className="text-blue-300 underline"
          onClick={() => setSelectedTask(task)}
        >
          Ver más
        </button>
        <div className="space-x-2">
          {isFree && (
            <button
              onClick={() => handleTake(task)}
              className="rounded-full border border-white px-2 py-1 text-white hover:bg-white hover:text-black"
            >
              Tomar Task
            </button>
          )}
          {task.status === "ASSIGNED" && (
            <button
              onClick={() => handleStart(task)}
              className="rounded-full border border-white px-2 py-1 text-white hover:bg-white hover:text-black"
            >
              Start
            </button>
          )}
          {task.status === "IN_PROGRESS" && (
            <>
              <button
                onClick={() => handleStop(task)}
                className="rounded-full border border-white px-2 py-1 text-white hover:bg-white hover:text-black"
              >
                Parar
              </button>
              <button
                onClick={() => handleComplete(task)}
                className="rounded-full border border-green-500 px-2 py-1 text-green-500 hover:bg-green-500 hover:text-white"
              >
                Completar
              </button>
            </>
          )}
          {task.status === "COMPLETED" && (
            <button
              onClick={() => handleUndo(task)}
              className="rounded-full border border-white px-2 py-1 text-white hover:bg-white hover:text-black"
            >
              Undo
            </button>
          )}
        </div>
      </div>
    </div>
  );

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-3xl font-bold text-white">
          Tareas del Sprint {sprintId}
        </h1>
        <NewTaskModal
          projectId={projectId}
          sprintId={sprintId}
          onCreated={loadTasks}
        />
      </div>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <section className="bg-black bg-opacity-10 rounded-xl p-4">
          <h2 className="text-xl font-bold mb-2 text-white">Free Team Tasks</h2>
          {freeTasks.length ? (
            freeTasks.map((t) => renderCard(t, true))
          ) : (
            <p className="text-gray-400">No hay tareas libres</p>
          )}
        </section>

        <section className="bg-black bg-opacity-10 rounded-xl p-4">
          <h2 className="text-xl font-bold mb-2 text-white">
            My Assigned Tasks
          </h2>
          {myAssigned.length ? (
            myAssigned.map((t) => renderCard(t, false))
          ) : (
            <p className="text-gray-400">No tienes tareas asignadas</p>
          )}
        </section>

        <section className="bg-black bg-opacity-10 rounded-xl p-4">
          <h2 className="text-xl font-bold mb-2 text-white">
            In Progress Tasks
          </h2>
          {inProgress.length ? (
            inProgress.map((t) => renderCard(t, false))
          ) : (
            <p className="text-gray-400">No hay tareas en progreso</p>
          )}
        </section>

        <section className="bg-black bg-opacity-10 rounded-xl p-4">
          <h2 className="text-xl font-bold mb-2 text-white">Completed Tasks</h2>
          {completed.length ? (
            completed.map((t) => renderCard(t, false))
          ) : (
            <p className="text-gray-400">No hay tareas completadas</p>
          )}
        </section>
      </div>

      {/* Modal de detalles */}
      {selectedTask && (
        <TaskModal task={selectedTask} onClose={() => setSelectedTask(null)} />
      )}
    </div>
  );
}
