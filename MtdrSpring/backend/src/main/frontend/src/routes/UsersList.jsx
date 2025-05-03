// src/routes/UsersList.js
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import config from "../../config";

/** Íconos de ejemplo. Reemplaza estos componentes con los íconos de tu elección */
const IconEdit = () => <span>✎</span>;
const IconTrash = () => <span>🗑</span>;
const IconCheck = () => <span>✓</span>;
const IconCancel = () => <span>✕</span>;

/**
 * Componente que representa una fila de una skill existente.
 * Maneja internamente su estado de edición.
 */
function SkillRow({
  skill,
  isEditing,
  defaultName,
  defaultDesc,
  onStartEdit,
  onCancelEdit,
  onSaveEdit,
  onDelete,
}) {
  const [localName, setLocalName] = useState(defaultName);
  const [localDesc, setLocalDesc] = useState(defaultDesc);

  const handleSave = () => {
    onSaveEdit(skill.idSkills, localName, localDesc);
  };

  if (!isEditing) {
    return (
      <tr className="border-b border-gray-700">
        <td className="px-4 py-2">{skill.name}</td>
        <td className="px-4 py-2">{skill.description}</td>
        <td className="px-4 py-2 space-x-2">
          <button
            onClick={onStartEdit}
            className="rounded-full bg-customDark bg-opacity-70 p-2 hover:bg-gray-500"
            title="Editar Skill"
          >
            <IconEdit />
          </button>
          <button
            onClick={() => onDelete(skill.idSkills)}
            className="rounded-full bg-customDark bg-opacity-70 p-2 hover:bg-gray-500"
            title="Eliminar Skill"
          >
            <IconTrash />
          </button>
        </td>
      </tr>
    );
  } else {
    return (
      <tr className="border-b border-gray-700">
        <td className="px-4 py-2">
          <input
            className="w-full rounded-md p-1 bg-customDark bg-opacity-70 text-white"
            value={localName}
            onChange={(e) => setLocalName(e.target.value)}
          />
        </td>
        <td className="px-4 py-2">
          <input
            className="w-full rounded-md p-1 bg-customDark bg-opacity-70 text-white"
            value={localDesc}
            onChange={(e) => setLocalDesc(e.target.value)}
          />
        </td>
        <td className="px-4 py-2 space-x-2">
          <button
            onClick={handleSave}
            className="rounded-full  bg-green-700 p-2 hover:bg-green-600"
            title="Guardar cambios"
          >
            <IconCheck />
          </button>
          <button
            onClick={onCancelEdit}
            className="rounded-full bg-red-700 p-2 hover:bg-red-600"
            title="Cancelar edición"
          >
            <IconCancel />
          </button>
        </td>
      </tr>
    );
  }
}

/**
 * Componente que representa una fila para agregar una nueva skill.
 */
function NewSkillRow({ onSave, onCancel }) {
  const [name, setName] = useState("");
  const [desc, setDesc] = useState("");

  const handleSave = () => {
    if (!name || !desc) {
      alert("Completa el nombre y la descripción");
      return;
    }
    onSave(name, desc);
  };

  return (
    <tr className="border-b border-gray-700 bg-green-900/30">
      <td className="px-4 py-2">
        <input
          className="w-full rounded-md p-1 bg-gray-700 text-white"
          placeholder="Nombre de la skill"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </td>
      <td className="px-4 py-2">
        <input
          className="w-full rounded-md p-1 bg-gray-700 text-white"
          placeholder="Descripción"
          value={desc}
          onChange={(e) => setDesc(e.target.value)}
        />
      </td>
      <td className="px-4 py-2 space-x-2">
        <button
          onClick={handleSave}
          className="rounded-full bg-green-700 p-2 hover:bg-green-600"
          title="Guardar nueva skill"
        >
          <IconCheck />
        </button>
        <button
          onClick={onCancel}
          className="rounded-full bg-red-700 p-2 hover:bg-red-600"
          title="Cancelar"
        >
          <IconCancel />
        </button>
      </td>
    </tr>
  );
}

/**
 * Componente principal: muestra la lista de usuarios y permite ver las skills de cada uno en un modal.
 * Se incorpora un botón de "Go Back" para regresar a los sprints del proyecto.
 */
function UsersList() {
  const { projectId } = useParams() || { projectId: "41" };
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Manejo del modal para ver y editar skills
  const [showModal, setShowModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [editingSkillId, setEditingSkillId] = useState(null);
  const [newSkillRow, setNewSkillRow] = useState(false);

  useEffect(() => {
    // Obtener la lista de usuarios del proyecto
    fetch(`${config.apiBaseUrl}/api/project-users/project/${projectId}/users`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Error al obtener los usuarios del proyecto");
        }
        return response.json();
      })
      .then((usersData) => {
        return Promise.all(
          usersData.map((user) =>
            fetch(
              `${config.apiBaseUrl}/api/project-users/role-user/project-id/${projectId}/user-id/${user.idUser}`,
            )
              .then((respRole) => {
                if (!respRole.ok) {
                  throw new Error("Error al obtener el rol de usuario");
                }
                return respRole.text();
              })
              .then((roleText) =>
                fetch(
                  `${config.apiBaseUrl}/api/skills/oracleuser/${user.idUser}`,
                )
                  .then((respSkill) => {
                    if (!respSkill.ok) {
                      throw new Error(
                        "Error al obtener las skills del usuario",
                      );
                    }
                    return respSkill.json();
                  })
                  .then((skillsData) => ({
                    ...user,
                    role: roleText.trim(),
                    skills: Array.isArray(skillsData) ? skillsData : [],
                  })),
              ),
          ),
        );
      })
      .then((usersWithDetails) => {
        setUsers(usersWithDetails);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, [projectId]);

  // Abre el modal con el usuario seleccionado
  const openModal = (user) => {
    setSelectedUser(user);
    setShowModal(true);
    setEditingSkillId(null);
    setNewSkillRow(false);
  };

  const closeModal = () => {
    setSelectedUser(null);
    setShowModal(false);
    setEditingSkillId(null);
    setNewSkillRow(false);
  };

  // Funciones para editar skills
  const startEditSkill = (skillId) => {
    setEditingSkillId(skillId);
    setNewSkillRow(false);
  };
  const cancelEditSkill = () => {
    setEditingSkillId(null);
  };
  const saveEditSkill = async (skillId, newName, newDesc) => {
    if (!selectedUser) return;
    try {
      const response = await fetch(
        `${config.apiBaseUrl}/api/skills/${skillId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ name: newName, description: newDesc }),
        },
      );
      if (!response.ok) {
        throw new Error("Error al actualizar la skill");
      }
      const updatedSkill = await response.json();
      setUsers((prevUsers) =>
        prevUsers.map((user) => {
          if (user.idUser === selectedUser.idUser) {
            const updatedSkills = user.skills.map((s) =>
              s.idSkills === skillId ? { ...s, ...updatedSkill } : s,
            );
            return { ...user, skills: updatedSkills };
          }
          return user;
        }),
      );
      setEditingSkillId(null);
    } catch (err) {
      alert(err.message);
    }
  };

  const deleteSkill = async (skillId) => {
    if (!selectedUser) return;
    const confirmDel = window.confirm("¿Eliminar esta skill?");
    if (!confirmDel) return;
    try {
      const response = await fetch(
        `${config.apiBaseUrl}/api/skills/${skillId}`,
        {
          method: "DELETE",
        },
      );
      if (!response.ok) {
        throw new Error("Error al eliminar la skill");
      }
      setUsers((prevUsers) =>
        prevUsers.map((user) => {
          if (user.idUser === selectedUser.idUser) {
            const filtered = user.skills.filter((s) => s.idSkills !== skillId);
            return { ...user, skills: filtered };
          }
          return user;
        }),
      );
    } catch (err) {
      alert(err.message);
    }
  };

  // Funciones para agregar skill
  const addSkillRow = () => {
    setNewSkillRow(true);
    setEditingSkillId(null);
  };

  const cancelAddSkill = () => {
    setNewSkillRow(false);
  };

  const saveNewSkill = async (newName, newDesc) => {
    if (!selectedUser) return;
    const payload = {
      oracleUser: { idUser: selectedUser.idUser },
      name: newName,
      description: newDesc,
    };
    try {
      const response = await fetch(`${config.apiBaseUrl}/api/skills`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });
      if (!response.ok) {
        throw new Error("Error al crear la skill");
      }
      const createdSkill = await response.json();
      setUsers((prevUsers) =>
        prevUsers.map((user) => {
          if (user.idUser === selectedUser.idUser) {
            return { ...user, skills: [...user.skills, createdSkill] };
          }
          return user;
        }),
      );
      setNewSkillRow(false);
    } catch (err) {
      alert(err.message);
    }
  };

  if (loading) {
    return <p className="text-center mt-8 text-white">Cargando usuarios...</p>;
  }
  if (error) {
    return <p className="text-center mt-8 text-red-500">{error}</p>;
  }

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4 text-white">Lista de Usuarios</h1>
      <table className="min-w-full bg-black bg-opacity-20 rounded-2xl overflow-hidden">
        <thead className="bg-black bg-opacity-30">
          <tr>
            <th className="px-4 py-2 text-left text-white">ID</th>
            <th className="px-4 py-2 text-left text-white">Nombre</th>
            <th className="px-4 py-2 text-left text-white">Rol</th>
            <th className="px-4 py-2 text-left text-white">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.idUser} className="border-b border-gray-600">
              <td className="px-4 py-2 text-white">{u.idUser}</td>
              <td className="px-4 py-2 text-white">{u.name}</td>
              <td className="px-4 py-2 text-white">{u.role}</td>
              <td className="px-4 py-2">
                <button
                  onClick={() => openModal(u)}
                  className="rounded-full border border-gray-300 px-3 py-1 hover:bg-gray-700 text-white"
                >
                  Ver Skills
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Modal para ver y editar Skills */}
      {showModal && selectedUser && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center "
          onClick={closeModal}
        >
          <div
            className="max-w-2xl w-full p-4 bg-customDark bg-opacity-90 text-white rounded-2xl relative"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="text-xl font-bold mb-4">
              Skills de {selectedUser.name}
            </h2>
            <button
              onClick={closeModal}
              className="absolute top-4 right-4 text-xl bg-transparent cursor-pointer"
            >
              <IconCancel />
            </button>
            <table className="w-full bg-customDark bg-opacity-70 rounded-2xl overflow-hidden">
              <thead className="bg-customDark bg-opacity-60">
                <tr>
                  <th className="px-4 py-2 text-left">Nombre</th>
                  <th className="px-4 py-2 text-left">Descripción</th>
                  <th className="px-4 py-2 text-left">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {selectedUser.skills.map((skill) => (
                  <SkillRow
                    key={skill.idSkills}
                    skill={skill}
                    isEditing={skill.idSkills === editingSkillId}
                    defaultName={skill.name}
                    defaultDesc={skill.description}
                    onStartEdit={() => startEditSkill(skill.idSkills)}
                    onCancelEdit={cancelEditSkill}
                    onSaveEdit={saveEditSkill}
                    onDelete={deleteSkill}
                  />
                ))}
                {newSkillRow && (
                  <NewSkillRow
                    onSave={saveNewSkill}
                    onCancel={cancelAddSkill}
                  />
                )}
              </tbody>
            </table>
            {!newSkillRow && (
              <button
                onClick={addSkillRow}
                className="mt-4 rounded-full border border-green-500 text-green-500 px-3 py-1 hover:bg-green-500 hover:text-white"
              >
                Agregar Skill
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default UsersList;
