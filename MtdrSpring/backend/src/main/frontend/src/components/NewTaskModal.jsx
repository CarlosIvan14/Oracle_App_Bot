// src/components/NewTaskModal.jsx
import React, { useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

/**
 * Props
 * • projectId   – ID del proyecto actual
 * • sprintId    – ID del sprint actual
 * • onCreated() – callback para recargar listas en SprintTasks
 */
export default function NewTaskModal({ projectId, sprintId, onCreated }) {
  /* ───────── toggle modal ───────── */
  const [open, setOpen] = useState(false);

  /* ───────── campos de la tarea ───────── */
  const [name,        setName]        = useState('');
  const [description, setDescription] = useState('');
  const [storyPoints, setStoryPoints] = useState(1);
  const [estimated,   setEstimated]   = useState(1);
  const [deadline,    setDeadline]    = useState(null);

  /* ───────── info de usuario / rol ───────── */
  const user = JSON.parse(localStorage.getItem('user'));
  const [role,            setRole]            = useState(null);   // 'manager' | 'developer'
  const [myProjectUserId, setMyProjectUserId] = useState(null);   // para auto‑asignar
  const [sending,         setSending]         = useState(false);

  /* ───────── manager only ───────── */
  const [mode, setMode] = useState('FREE');                       // FREE | ASSIGN | AI
  const [allUsers,       setAllUsers]       = useState([]);       // users del proyecto
  const [selectedUserId, setSelectedUserId] = useState('');       // idUser
  /* IA */
  const [aiUsers,   setAiUsers]   = useState([]);                 // lista ordenada
  const [aiLoading, setAiLoading] = useState(false);

  /* ════════════════════════════════════════
     Al abrir el modal refrescar datos
  ════════════════════════════════════════ */
  useEffect(() => {
    if (!open) return;

    /* 1. Rol */
    fetch(`http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${user.idUser}`)
      .then(r => r.ok ? r.text() : Promise.reject())
      .then(txt => setRole(txt.trim()))
      .catch(()  => setRole(null));

    /* 2. mi idProjectUser (dev) */
    fetch(`http://localhost:8081/api/project-users/project-user-id/project-id/${projectId}/user-id/${user.idUser}`)
      .then(r => r.ok ? r.text() : null)
      .then(txt => setMyProjectUserId(txt ? Number(txt) : null))
      .catch(() => setMyProjectUserId(null));

    /* 3. lista de usuarios del proyecto */
    fetch(`http://localhost:8081/api/project-users/project/${projectId}/users`)
      .then(r => r.ok ? r.json() : [])
      .then(setAllUsers)
      .catch(() => setAllUsers([]));

    /* clear */
    setSelectedUserId('');
    setMode('FREE');
    setAiUsers([]);
  }, [open, projectId, user.idUser]);

  /* ════════════════════════════════════════
     Obtener recomendación IA
  ════════════════════════════════════════ */
  const fetchAiRecommendation = async () => {
    if (!name || !description) {
      return alert('Primero llena nombre y descripción de la tarea');
    }
    setAiLoading(true);
    try {
      const res = await fetch('http://localhost:8081/assignment/by-ai', {
        method : 'POST',
        headers: { 'Content-Type':'application/json' },
        body   : JSON.stringify({ projectId, name, description })
      });
      if (!res.ok) throw new Error();
      const list = await res.json();     // [{idUser, name, ...}]
      setAiUsers(list);
      setSelectedUserId('');             // reset selección
    } catch {
      alert('Error obteniendo recomendación IA');
    } finally {
      setAiLoading(false);
    }
  };

  /* ════════════════════════════════════════
     Crear tarea (y assignee si aplica)
  ════════════════════════════════════════ */
  const handleCreate = async () => {
    if (sending) return;
    if (!name || !description || !deadline)
      return alert('Completa nombre, descripción y deadline');

    // manager + ASSIGN/AI necesitan usuario seleccionado
    if (role==='manager' && ['ASSIGN','AI'].includes(mode) && !selectedUserId)
      return alert('Selecciona un usuario al que asignar');

    setSending(true);

    /* 1) crear Tarea */
    const taskPayload = {
      name,
      status : role==='manager'
               ? (mode==='FREE' ? 'UNASSIGNED' : 'ASSIGNED')
               : 'ASSIGNED',
      description,
      deadline      : deadline.toISOString(),
      storyPoints   : Number(storyPoints),
      sprint        : { id_sprint : Number(sprintId) },
      creation_ts   : new Date().toISOString(),
      realHours     : 0,
      estimatedHours: Number(estimated)
    };

    let createdTask;
    try {
      const res = await fetch('http://localhost:8081/api/tasks', {
        method :'POST',
        headers:{ 'Content-Type':'application/json' },
        body   : JSON.stringify(taskPayload)
      });
      if (!res.ok) throw new Error();
      createdTask = await res.json();
    } catch {
      setSending(false);
      return alert('Error creando la tarea');
    }

    /* 2) ¿assignee? */
    const needAssignee =
      role==='developer' || (role==='manager' && mode!=='FREE');

    if (needAssignee) {
      /* obtener idProjectUser */
      let idProjectUser = null;

      if (role==='developer') {
        idProjectUser = myProjectUserId;
      } else {
        // manager
        try {
          const resPU = await fetch(
            `http://localhost:8081/api/project-users/project-id/${projectId}/user-id/${selectedUserId}`
          );
          if (resPU.ok) {
            const txt = await resPU.text();
            idProjectUser = txt ? Number(txt) : null;
          }
        } catch {/* ignore */}
      }

      if (idProjectUser) {
        try {
          await fetch('http://localhost:8081/api/task-assignees', {
            method :'POST',
            headers:{ 'Content-Type':'application/json' },
            body   : JSON.stringify({
              projectUser:{ idProjectUser },
              task      :{ id: createdTask.id }
            })
          });
        } catch {
          alert('Tarea creada, pero falló la asignación automática.');
        }
      }
    }

    /* 3) limpiar y cerrar */
    setSending(false);
    setOpen(false);
    setName('');
    setDescription('');
    setStoryPoints(1);
    setEstimated(1);
    setDeadline(null);
    setSelectedUserId('');
    setAiUsers([]);

    onCreated && onCreated();
  };

  /* ───────── helpers UI ───────── */
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

  /* ════════════════════════════════════════
     Render
  ════════════════════════════════════════ */
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
        <div className="fixed inset-0 z-50 flex items-center justify-center "
             onClick={() => !sending && setOpen(false)}>
          <div
            className="max-w-lg w-full bg-customDark  text-white p-6 rounded-2xl relative"
            onClick={e => e.stopPropagation()}
          >
            <button className="absolute top-3 right-3" onClick={() => !sending && setOpen(false)}>✕</button>
            <h2 className="text-2xl font-bold mb-4 text-center">Nueva Tarea</h2>

            {role==='manager' && (
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
                className="w-full bg-customDarkligth bg-opac rounded-lg p-2"
                value={name}
                onChange={e => setName(e.target.value)}
              />
              <textarea
                rows={3}
                placeholder="Descripción"
                className="w-full bg-customDarkligth bg-opac rounded-lg p-2 resize-none"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />

              <div className="grid grid-cols-2 gap-3">
                <label className="flex flex-col text-sm">
                  Story Points
                  <input
                    type="number" min={1} max={10}
                    className="bg-customDarkligth bg-opac rounded-lg p-2 mt-1"
                    value={storyPoints}
                    onChange={e => setStoryPoints(e.target.value)}
                  />
                </label>
                <label className="flex flex-col text-sm">
                  Horas estimadas
                  <input
                    type="number" min={1}
                    className="bg-customDarkligth bg-opac rounded-lg p-2 mt-1"
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
                className="w-full bg-customDarkligth bg-opac rounded-lg p-2 text-center"
                wrapperClassName="w-full"
              />

              {/* ASSIGN */}
              {role==='manager' && mode==='ASSIGN' && (
                <select
                  value={selectedUserId}
                  onChange={e => setSelectedUserId(e.target.value)}
                  className="w-full bg-customDarkligth bg-opac rounded-lg p-2"
                >
                  <option value="">— Selecciona usuario —</option>
                  {allUsers.map(u => (
                    <option key={u.idUser} value={u.idUser}>
                      {u.name} ({u.roleUser})
                    </option>
                  ))}
                </select>
              )}

              {/* IA */}
              {role==='manager' && mode==='AI' && (
                <>
                  {aiUsers.length === 0 ? (
                    <button
                      type="button"
                      disabled={aiLoading}
                      onClick={fetchAiRecommendation}
                      className={`w-full rounded-full border py-1 ${
                        aiLoading
                          ? 'border-gray-600 text-gray-500 cursor-wait'
                          : 'border-cyan-500 hover:bg-cyan-500 hover:text-white transition'
                      }`}
                    >
                      {aiLoading ? 'Consultando IA…' : 'Obtener recomendación IA'}
                    </button>
                  ) : (
                    <select
                      value={selectedUserId}
                      onChange={e => setSelectedUserId(e.target.value)}
                      className="w-full bg-customDarkligth bg-opac rounded-lg p-2"
                    >
                      <option value="">— Selecciona usuario recomendado —</option>
                      {aiUsers.map(u => (
                        <option key={u.idUser} value={u.idUser}>
                          {u.name} ({u.roleUser})
                        </option>
                      ))}
                    </select>
                  )}
                </>
              )}

            </div>

            {/* crear */}
            <button
              disabled={sending}
              onClick={handleCreate}
              className={`mt-6 w-full rounded-full py-2 border
                ${sending
                  ? 'border-gray-600 text-gray-500 cursor-not-allowed'
                  : 'border-cyan-500 hover:bg-cyan-500 hover:text-white transition'}`}
            >
              {sending ? 'Creando…' : 'Crear Tarea'}
            </button>
          </div>
        </div>
      )}
    </>
  );
}
