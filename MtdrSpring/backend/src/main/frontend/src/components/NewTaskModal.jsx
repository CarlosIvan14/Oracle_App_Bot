import React, { useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

/**
 * Props
 * ──────────────────────────────────────────────────────────
 * • projectId   → ID del proyecto actual
 * • sprintId    → ID del sprint actual
 * • onCreated() → callback para recargar listas en SprintTasks
 */
export default function NewTaskModal({ projectId, sprintId, onCreated }) {
  /* ───────── toggle modal ───────── */
  const [open, setOpen] = useState(false);

  /* ───────── campos de la tarea ───────── */
  const [name,         setName]         = useState('');
  const [description,  setDescription]  = useState('');
  const [storyPoints,  setStoryPoints]  = useState(1);
  const [estimated,    setEstimated]    = useState(1);
  const [deadline,     setDeadline]     = useState(null);

  /* ───────── info de usuario / rol ───────── */
  const user = JSON.parse(localStorage.getItem('user'));          // ← ya existe en localStorage
  const [role,            setRole]            = useState(null);   // 'manager' | 'developer'
  const [myProjectUserId, setMyProjectUserId] = useState(null);   // para auto‑asignar

  /* ───────── manager only ───────── */
  const [mode, setMode] = useState('FREE');                       // FREE | ASSIGN | AI
  const [allUsers, setAllUsers]       = useState([]);             // lista de usuarios   (idUser, name…)
  const [selectedUserId, setSelected] = useState('');             // idUser elegido en el <select>

  /* ════════════════════════════════════════════════════════════════
     Al ABRIR el modal refrescamos:
       1) rol del usuario
       2) su idProjectUser
       3) todos los usuarios del proyecto (para managers)
  ════════════════════════════════════════════════════════════════ */
  useEffect(() => {
    if (!open) return;

    /* 1. Rol */
    fetch(`http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${user.idUser}`)
      .then(r => r.ok ? r.text() : Promise.reject())
      .then(txt => setRole(txt.trim()))
      .catch(() => setRole(null));

    /* 2. Mi idProjectUser (para developers) */
    fetch(`http://localhost:8081/api/project-users/project-user-id/project-id/${projectId}/user-id/${user.idUser}`)
      .then(r => r.ok ? r.json() : null)
      .then(setMyProjectUserId)
      .catch(() => setMyProjectUserId(null));

    /* 3. Lista de usuarios (nombre + idUser) — NO contiene idProjectUser */
    fetch(`http://localhost:8081/api/project-users/project/${projectId}/users`)
      .then(r => r.ok ? r.json() : [])
      .then(setAllUsers)
      .catch(() => setAllUsers([]));

    /* reset de controles */
    setSelected('');
    setMode('FREE');
  }, [open, projectId, user.idUser]);

  /* ════════════════════════════════════════════════════════════════
     CREAR TAREA + (si corresponde) ASSIGNEE
  ════════════════════════════════════════════════════════════════ */
  const handleCreate = async () => {
    /* ─── validaciones mínimas ─── */
    if (!name || !description || !deadline)
      return alert('Completa nombre, descripción y deadline');
    if (role === 'manager' && mode === 'ASSIGN' && !selectedUserId)
      return alert('Selecciona un usuario al que asignar');

    /* ─── 1) crear la tarea ─── */
    const taskPayload = {
      name,
      status        : role === 'manager'
                        ? (mode === 'FREE' ? 'UNASSIGNED' : 'ASSIGNED')
                        : 'ASSIGNED',
      description,
      deadline      : deadline.toISOString(),
      storyPoints   : Number(storyPoints),
      sprint        : { idsprint: Number(sprintId) },
      creation_ts   : new Date().toISOString(),
      realHours     : 0,
      estimatedHours: Number(estimated)
    };

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

    /* ─── 2) ¿hay que crear task‑assignee? ─── */
    const needAssignee =
      (role === 'manager' && mode === 'ASSIGN') ||
      role === 'developer';

    if (needAssignee) {
      /* 2.a) determinar idProjectUser correcto                          */
      let idProjectUser = null;

      if (role === 'developer') {
        idProjectUser = myProjectUserId;
      } else {                       // manager + ASSIGN
        try {
          const resPU = await fetch(
            `http://localhost:8081/api/project-users/project-user-id/project-id/${projectId}/user-id/${selectedUserId}`
          );
          if (resPU.ok) idProjectUser = await resPU.json();
        } catch {/* ignore */}
      }

      /* 2.b) POST a task‑assignees                                      */
      if (idProjectUser) {
        try {
          await fetch('http://localhost:8081/api/task-assignees', {
            method : 'POST',
            headers: { 'Content-Type':'application/json' },
            body   : JSON.stringify({
              projectUser: { idProjectUser },
              task       : { id: createdTask.id }
            })
          });
        } catch {
          alert('La tarea se creó, pero falló la asignación automática.');
        }
      }
    }

    /* ─── 3) limpiar y notificar ─── */
    setOpen(false);
    setName(''); setDescription('');
    setStoryPoints(1); setEstimated(1);
    setDeadline(null); setSelected('');

    onCreated && onCreated();
  };

  /* ════════════════════════════════════════════════════════════════
     UI
  ════════════════════════════════════════════════════════════════ */
  const BtnMode = ({ value, children }) => (
    <button
      type="button"
      onClick={() => setMode(value)}
      className={`rounded-full px-3 py-1 border text-sm transition
        ${mode===value
          ? 'bg-cyan-500 text-white border-cyan-500'
          : 'border-gray-500 text-gray-300 hover:bg-cyan-500 hover:text-white'}`}
    >
      {children}
    </button>
  );

  return (
    <>
      {/* trigger */}
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

      {/* modal */}
      {open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60"
             onClick={() => setOpen(false)}>
          <div
            className="max-w-lg w-full bg-gray-900 text-white p-6 rounded-2xl relative"
            onClick={e => e.stopPropagation()}
          >
            <button className="absolute top-3 right-3" onClick={() => setOpen(false)}>✕</button>
            <h2 className="text-2xl font-bold mb-4 text-center">Nueva Tarea</h2>

            {/* modos (manager) */}
            {role === 'manager' && (
              <div className="flex justify-center mb-4 space-x-2">
                <BtnMode value="FREE">Free Task</BtnMode>
                <BtnMode value="ASSIGN">Asignar usuario</BtnMode>
                <BtnMode value="AI">Recomendación IA</BtnMode>
              </div>
            )}

            {/* formulario */}
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

              {/* selector usuario (manager + ASSIGN) */}
              {role === 'manager' && mode === 'ASSIGN' && (
                <select
                  value={selectedUserId}
                  onChange={e => setSelected(e.target.value)}
                  className="w-full bg-gray-700 rounded-lg p-2"
                >
                  <option value="">— Selecciona usuario —</option>
                  {allUsers.map(u => (
                    <option key={u.idUser} value={u.idUser}>
                      {u.name} ({u.roleUser})
                    </option>
                  ))}
                </select>
              )}

              {role === 'manager' && mode === 'AI' && (
                <div className="text-center text-sm text-yellow-400">
                  Próximamente: asignación inteligente.
                </div>
              )}
            </div>

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
