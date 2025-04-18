import React, { useState, useEffect } from 'react';
import DatePicker             from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

/**
 * Props:
 *  • projectId   – ID del proyecto activo
 *  • sprintId    – ID del sprint activo
 *  • onCreated() – callback para recargar las listas en SprintTasks
 *
 * El componente refresca el **rol** del usuario cada vez que se abre
 * el modal, de modo que siempre muestra los botones correctos.
 */
export default function NewTaskModal({ projectId, sprintId, onCreated }) {
  /* ───────── estados del modal ───────── */
  const [open       , setOpen]       = useState(false);

  /* campos comunes de la tarea */
  const [name       , setName]       = useState('');
  const [description, setDescription] = useState('');
  const [storyPoints, setStoryPoints] = useState(1);
  const [estimated  , setEstimated]   = useState(1);
  const [deadline   , setDeadline]    = useState(null);

  /* datos relativos al usuario y rol ##################################### */
  const user = JSON.parse(localStorage.getItem('user'));  // ya validado arriba en la app
  const [role            , setRole]            = useState(null);   // 'manager' | 'developer'
  const [myProjectUserId , setMyProjectUserId] = useState(null);

  /* manager‑only */
  const [mode      , setMode]       = useState('FREE'); // FREE | ASSIGN | AI
  const [allUsers  , setAllUsers]   = useState([]);     // project users
  const [selectedPU, setSelectedPU] = useState('');     // idProjectUser

  /* ════════════════════════════════════════════════════════════════════ */
  /*                   Cargar ROL + projectUserId                        */
  /* ════════════════════════════════════════════════════════════════════ */

  /** Siempre que ABRIMOS el modal refrescamos los datos del usuario   */
  useEffect(() => {
    if (!open) return;

    /* 1. Rol del usuario en este proyecto */
    fetch(`http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${user.idUser}`)
      .then(r => r.ok ? r.text() : Promise.reject())
      .then(txt => setRole(txt.trim()))
      .catch(()  => setRole(null));

    /* 2. Si es developer, necesitamos su idProjectUser                  */
    fetch(`http://localhost:8081/api/project-users/project-user-id/project-id/${projectId}/user-id/${user.idUser}`)
      .then(r => r.ok ? r.json() : null)
      .then(id => setMyProjectUserId(id))
      .catch(() => setMyProjectUserId(null));

    /* 3. Limpiar caches por si el rol cambió                            */
    setAllUsers([]);
    setSelectedPU('');
    setMode('FREE');
  }, [open, projectId, user.idUser]);

  /** Cargar TODOS los usuarios del proyecto (sólo managers)            */
  useEffect(() => {
    if (open && role === 'manager' && allUsers.length === 0) {
      fetch(`http://localhost:8081/api/project-users/project/${projectId}/users`)
        .then(r => r.ok ? r.json() : [])
        .then(setAllUsers)
        .catch(() => setAllUsers([]));
    }
  }, [open, role, allUsers.length, projectId]);

  /* ════════════════════════════════════════════════════════════════════ */
  /*                             Crear tarea                             */
  /* ════════════════════════════════════════════════════════════════════ */

  const handleCreate = async () => {
    /* Validaciones mínimas */
    if (!name || !description || !deadline)          return alert('Completa nombre, descripción y deadline');
    if (role === 'manager' && mode === 'ASSIGN' && !selectedPU) return alert('Selecciona un usuario');

    /* 1. Construimos el payload de la tarea */
    const taskPayload = {
      name,
      status        : role === 'manager'
                      ? (mode === 'FREE' ? 'UNASSIGNED' : 'ASSIGNED')
                      : 'ASSIGNED',
      description,
      deadline      : deadline.toISOString(),
      storyPoints   : Number(storyPoints),
      sprint        : { id_sprint: Number(sprintId) },
      creation_ts   : new Date().toISOString(),
      realHours     : 0,
      estimatedHours: Number(estimated)
    };

    /* 2. Creamos la tarea */
    let createdTask;
    try {
      const res = await fetch('http://localhost:8081/api/tasks', {
        method : 'POST',
        headers: { 'Content-Type':'application/json' },
        body   : JSON.stringify(taskPayload)
      });
      if (!res.ok) throw new Error();
      createdTask = await res.json();
    } catch {
      return alert('Error creando la tarea');
    }

    /* 3. Determinar si hay que crear un assignee ************************/
    const mustCreateAssignee =
      // Manager → modo ASSIGN
      (role === 'manager' && mode === 'ASSIGN') ||
      // Developer → se auto‑asigna
      (role === 'developer');

    if (mustCreateAssignee) {
      const projectUserToAssign =
        role === 'manager' ? Number(selectedPU) : Number(myProjectUserId);

      if (projectUserToAssign) {
        try {
          await fetch('http://localhost:8081/api/task-assignees', {
            method : 'POST',
            headers: { 'Content-Type':'application/json' },
            body   : JSON.stringify({
              projectUser: { idProjectUser: projectUserToAssign },
              task       : { id: createdTask.id }
            })
          });
        } catch {
          alert('La tarea se creó, pero no se pudo asignar automáticamente.');
        }
      }
    }

    /* 4. Cerrar, limpiar y avisar al padre */
    setOpen(false);
    setName(''); setDescription(''); setStoryPoints(1);
    setEstimated(1); setDeadline(null); setSelectedPU('');

    onCreated && onCreated();
  };

  /* ════════════════════════════════════════════════════════════════════ */
  /*                                UI                                   */
  /* ════════════════════════════════════════════════════════════════════ */

  const ActionBtn = ({ value, children }) => (
    <button
      type="button"
      onClick={() => setMode(value)}
      className={`rounded-full px-3 py-1 border text-sm
        ${mode===value
          ? 'bg-cyan-500 text-white border-cyan-500'
          : 'border-gray-500 text-gray-300 hover:bg-cyan-500 hover:text-white'}`}
    >
      {children}
    </button>
  );

  return (
    <>
      {/* Botón que abre el modal */}
      <button
        onClick={() => setOpen(true)}
        className="ml-4 flex items-center bg-transparent text-white font-semibold py-2 px-4 rounded-full
                   transition hover:scale-105 hover:border hover:border-cyan-500"
      >
        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" fill="none"
             viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        Añadir Tarea
      </button>

      {/* Modal */}
      {open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60"
             onClick={() => setOpen(false)}>

          <div
            className="max-w-lg w-full bg-gray-900 text-white p-6 rounded-2xl relative"
            onClick={e => e.stopPropagation()}
          >
            {/* Cerrar */}
            <button className="absolute top-3 right-3" onClick={() => setOpen(false)}>✕</button>

            <h2 className="text-2xl font-bold mb-4 text-center">Nueva Tarea</h2>

            {/* Botones modo (manager) */}
            {role === 'manager' && (
              <div className="flex justify-center mb-4 space-x-2">
                <ActionBtn value="FREE">Free Task</ActionBtn>
                <ActionBtn value="ASSIGN">Asignar usuario</ActionBtn>
                <ActionBtn value="AI">Recomendación IA</ActionBtn>
              </div>
            )}

            {/* Formulario principal */}
            <div className="space-y-3">
              <input
                placeholder="Nombre / título"
                className="w-full bg-gray-700 rounded-lg p-2"
                value={name}
                onChange={e => setName(e.target.value)}
              />

              <textarea
                rows={3}
                placeholder="Descripción"
                className="w-full bg-gray-700 rounded-lg p-2 resize-none"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />

              <div className="grid grid-cols-2 gap-3">
                <label className="flex flex-col text-sm">
                  Story Points
                  <input
                    type="number" min={1} max={10}
                    className="bg-gray-700 rounded-lg p-2 mt-1"
                    value={storyPoints}
                    onChange={e => setStoryPoints(e.target.value)}
                  />
                </label>
                <label className="flex flex-col text-sm">
                  Horas estimadas
                  <input
                    type="number" min={1}
                    className="bg-gray-700 rounded-lg p-2 mt-1"
                    value={estimated}
                    onChange={e => setEstimated(e.target.value)}
                  />
                </label>
              </div>

              <DatePicker
                selected={deadline}
                onChange={setDeadline}
                placeholderText="Deadline"
                dateFormat="yyyy-MM-dd"
                className="w-full bg-gray-700 rounded-lg p-2 text-center"
                wrapperClassName="w-full"
              />

              {/* Selector de usuario (manager + ASSIGN) */}
              {role === 'manager' && mode === 'ASSIGN' && (
                <select
                  value={selectedPU}
                  onChange={e => setSelectedPU(e.target.value)}
                  className="w-full bg-gray-700 rounded-lg p-2"
                >
                  <option value="">— Selecciona usuario —</option>
                  {allUsers.map(u => (
                    <option key={u.idProjectUser} value={u.idProjectUser}>
                      {u.name} ({u.role})
                    </option>
                  ))}
                </select>
              )}

              {/* Placeholder IA */}
              {role === 'manager' && mode === 'AI' && (
                <div className="text-center text-sm text-yellow-400">
                  Próximamente: asignación inteligente.
                </div>
              )}
            </div>

            {/* Botón crear */}
            <button
              onClick={handleCreate}
              className="mt-6 w-full rounded-full border border-cyan-500 py-2
                         hover:bg-cyan-500 hover:text-white transition"
            >
              Crear Tarea
            </button>
          </div>
        </div>
      )}
    </>
  );
}
