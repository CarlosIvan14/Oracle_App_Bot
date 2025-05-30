import React, { useState, useEffect } from "react";
import { FaUser } from "react-icons/fa";
import Home from "./Home";
import config from "../config";

/**
 * Profile.jsx – página de perfil con CRUD de Skills
 * -------------------------------------------------
 * • Hooks de estado al tope (cumple reglas)
 * • Carga inicial de skills vía API `/api/skills/oracleuser/:idUser`
 * • Layout grid: Izquierda → avatar + bio + detalles ; Derecha → Skills
 * • Modal de gestión: agregar, editar, eliminar skills
 * • Sin librerías externas adicionales, solo React y TailwindCSS
 */
export default function Profile({ user, onEdit }) {
  /* ----------------- hooks de estado ----------------- */
  const [skills, setSkills] = useState([]);
  const [loadingSkills, setLoadingSkills] = useState(true);
  const [showSkillModal, setShowSkillModal] = useState(false);
  const [editingSkillId, setEditingSkillId] = useState(null);
  const [newSkillRow, setNewSkillRow] = useState(false);

  /* ----------------- cargar skills del backend ----------------- */
  useEffect(() => {
    if (!user) return;
    setLoadingSkills(true);
    fetch(`${config.apiBaseUrl}/api/skills/oracleuser/${user.idUser}`)
      .then((r) => {
        if (!r.ok) throw new Error("Error al obtener skills");
        return r.json();
      })
      .then((data) => setSkills(Array.isArray(data) ? data : []))
      .catch(() => setSkills([]))
      .finally(() => setLoadingSkills(false));
  }, [user]);

  /* ----------------- early return si no hay usuario ----------------- */
  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-400">No user logged in.</p>
      </div>
    );
  }

  /* ----------------- CRUD handlers ----------------- */
  const startEditSkill = (id) => {
    setEditingSkillId(id);
    setNewSkillRow(false);
  };

  const cancelEditSkill = () => setEditingSkillId(null);

  const saveEditSkill = async (skillId, name, description) => {
    try {
      const resp = await fetch(`${config.apiBaseUrl}/api/skills/${skillId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, description }),
      });
      if (!resp.ok) throw new Error();
      const updated = await resp.json();
      setSkills((prev) => prev.map((s) => (s.idSkills === skillId ? { ...s, ...updated } : s)));
      setEditingSkillId(null);
    } catch {
      alert("Error al actualizar la skill");
    }
  };

  const deleteSkill = async (skillId) => {
    if (!window.confirm("¿Eliminar esta skill?")) return;
    try {
      const resp = await fetch(`${config.apiBaseUrl}/api/skills/${skillId}`, {
        method: "DELETE",
      });
      if (!resp.ok) throw new Error();
      setSkills((prev) => prev.filter((s) => s.idSkills !== skillId));
    } catch {
      alert("Error al eliminar la skill");
    }
  };

  const addSkillRow = () => {
    setNewSkillRow(true);
    setEditingSkillId(null);
  };
  const cancelAddSkill = () => setNewSkillRow(false);

  const saveNewSkill = async (name, description) => {
    const payload = {
      oracleUser: { idUser: user.idUser },
      name,
      description,
    };
    try {
      const resp = await fetch(`${config.apiBaseUrl}/api/skills`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!resp.ok) throw new Error();
      const created = await resp.json();
      setSkills((prev) => [...prev, created]);
      setNewSkillRow(false);
    } catch {
      alert("Error al crear la skill");
    }
  };

  /* ----------------- sub-componentes de tabla ----------------- */
  function SkillRow({ skill }) {
    const isEditing = editingSkillId === skill.idSkills;
    const [localName, setLocalName] = useState(skill.name);
    const [localDesc, setLocalDesc] = useState(skill.description);

    if (!isEditing) {
      return (
        <tr className="border-b border-gray-700 text-sm">
          <td className="px-4 py-2">{skill.name}</td>
          <td className="px-4 py-2">{skill.description}</td>
          <td className="px-4 py-2 space-x-2">
            <button onClick={() => startEditSkill(skill.idSkills)} className="bg-gray-700 hover:bg-gray-600 rounded-full p-1">✎</button>
            <button onClick={() => deleteSkill(skill.idSkills)} className="bg-gray-700 hover:bg-red-600 rounded-full p-1">🗑</button>
          </td>
        </tr>
      );
    }
    return (
      <tr className="border-b border-gray-700 text-sm bg-gray-800/40">
        <td className="px-4 py-2">
          <input className="w-full rounded-md p-1 bg-gray-700 text-white" value={localName} onChange={(e) => setLocalName(e.target.value)} />
        </td>
        <td className="px-4 py-2">
          <input className="w-full rounded-md p-1 bg-gray-700 text-white" value={localDesc} onChange={(e) => setLocalDesc(e.target.value)} />
        </td>
        <td className="px-4 py-2 space-x-2">
          <button onClick={() => saveEditSkill(skill.idSkills, localName, localDesc)} className="bg-green-700 hover:bg-green-600 rounded-full p-1">✓</button>
          <button onClick={cancelEditSkill} className="bg-red-700 hover:bg-red-600 rounded-full p-1">✕</button>
        </td>
      </tr>
    );
  }

  function NewSkillRow() {
    const [name, setName] = useState("");
    const [desc, setDesc] = useState("");
    return (
      <tr className="border-b border-gray-700 text-sm bg-green-900/30">
        <td className="px-4 py-2"><input className="w-full rounded-md p-1 bg-gray-700 text-white" placeholder="Nombre" value={name} onChange={(e) => setName(e.target.value)} /></td>
        <td className="px-4 py-2"><input className="w-full rounded-md p-1 bg-gray-700 text-white" placeholder="Descripción" value={desc} onChange={(e) => setDesc(e.target.value)} /></td>
        <td className="px-4 py-2 space-x-2">
          <button onClick={() => saveNewSkill(name, desc)} className="bg-green-700 hover:bg-green-600 rounded-full p-1">✓</button>
          <button onClick={cancelAddSkill} className="bg-red-700 hover:bg-red-600 rounded-full p-1">✕</button>
        </td>
      </tr>
    );
  }

  /* ----------------- datos de usuario ----------------- */
  const { name, role = "User", idUser, about = "No bio yet.", avatar } = user;
  const avatarNode = avatar ? (
    <img src={avatar} alt={name} className="w-24 h-24 rounded-full object-cover shadow-md" />
  ) : (
    <div className="w-24 h-24 rounded-full bg-gray-700 flex items-center justify-center shadow-md"><FaUser className="w-12 h-12 text-gray-300" /></div>
  );

  /* ----------------- render ----------------- */
  return (
    <section className="container mx-auto py-10 px-4">
      <header className="mb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight mb-1 text-gray-100">{name}</h1>
        <p className="text-gray-400 text-lg">{role}</p>
      </header>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Columna izquierda */}
        <div className="bg-gray-800/60 backdrop-blur rounded-2xl shadow-lg p-8 flex flex-col items-center md:col-span-1 w-full">
          {avatarNode}
          <h2 className="font-semibold text-xl mt-6 mb-2 text-gray-100">About me</h2>
          <p className="text-center text-sm text-gray-400 mb-6">{about}</p>
          {/* Detalles */}
          <div className="w-full space-y-2 text-sm text-gray-200">
            <div className="flex justify-between border-b border-gray-600 pb-1"><span>Full name</span><span className="text-gray-400">{name}</span></div>
            
          </div>
          {onEdit && <button onClick={onEdit} className="mt-6 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm">Edit profile</button>}
        </div>

        {/* Columna derecha – Skills */}
        <div className="bg-gray-800/60 backdrop-blur rounded-2xl shadow-lg p-8 md:col-span-2 w-full flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-xl text-gray-100">Skills</h2>
            <button onClick={() => setShowSkillModal(true)} className="px-3 py-1 rounded-full border border-gray-400 hover:bg-gray-700 text-gray-200 text-sm">Manage</button>
          </div>
          {/* chips o loader */}
          {loadingSkills ? (
            <p className="text-gray-400 text-sm">Cargando skills…</p>
          ) : skills.length > 0 ? (
            <ul className="flex flex-wrap gap-2">
              {skills.map((s, idx) => (
                <li
                  key={s.idSkills}
                  draggable
                  onDragStart={(e) => {
                    e.dataTransfer.setData("text/plain", idx);   // origen
                  }}
                  onDragOver={(e) => e.preventDefault()}         // permite soltar
                  onDrop={(e) => {
                    const from = Number(e.dataTransfer.getData("text/plain"));
                    const to   = idx;
                    if (from === to) return;
                    setSkills((prev) => {
                      const arr = [...prev];
                      const [moved] = arr.splice(from, 1);
                      arr.splice(to, 0, moved);
                      return arr;
                    });
                  }}
                  className="px-3 py-1 rounded-full bg-gray-700 text-xl hover:bg-gray-600 transition-colors duration-200 text-white
                            font-medium text-gray-300 cursor-grab active:cursor-grabbing"
                >
                  {s.name}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-400 text-sm">Aún no tienes skills añadidas. Pulsa Manage para agregarlas.</p>
          )}
        </div>
      </div>

      {/* sección Home */}
      <section className="py-10">
        <div className="bg-gray-800/60 backdrop-blur rounded-2xl shadow-lg p-8"><Home /></div>
      </section>

      {/* Modal gestión de skills */}
      {showSkillModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={() => setShowSkillModal(false)}>
          <div className="max-w-2xl w-full p-6 bg-[#212233] bg-opacity-95 rounded-2xl text-white relative" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-xl font-bold mb-4">Gestionar Skills</h2>
            <button className="absolute top-4 right-4 text-xl" onClick={() => setShowSkillModal(false)}>✕</button>
            <table className="w-full mb-4 text-sm">
              <thead><tr className="border-b border-gray-700"><th className="px-4 py-2 text-left">Nombre</th><th className="px-4 py-2 text-left">Descripción</th><th className="px-4 py-2 text-left">Acciones</th></tr></thead>
              <tbody>
                {skills.map((skill) => (<SkillRow key={skill.idSkills} skill={skill} />))}
                {newSkillRow && <NewSkillRow />}
              </tbody>
            </table>
            {!newSkillRow && <button onClick={addSkillRow} className="rounded-full border border-green-500 text-green-500 px-3 py-1 hover:bg-green-500 hover:text-white text-sm">Agregar Skill</button>}
          </div>
        </div>
      )}
    </section>
  );
}
