// src/routes/UsersList.js
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

function UsersList() {
  // Usamos el projectId de la ruta. Si no se proporciona, se usa un valor por defecto.
  const { projectId } = useParams() || { projectId: "41" };
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // Primero obtenemos la lista de usuarios del proyecto
    fetch(`http://localhost:8081/api/project-users/project/${projectId}/users`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Error al obtener los usuarios del proyecto");
        }
        return response.json();
      })
      .then((usersData) => {
        // Para cada usuario, obtenemos su rol y sus skills
        return Promise.all(
          usersData.map((user) =>
            // Obtener rol
            fetch(
              `http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${user.idUser}`
            )
              .then((respRole) => {
                if (!respRole.ok) {
                  throw new Error("Error al obtener el rol de usuario");
                }
                return respRole.text();
              })
              .then((roleText) =>
                // Obtener skills
                fetch(`http://localhost:8081/api/skills/oracleuser/${user.idUser}`)
                  .then((respSkill) => {
                    if (!respSkill.ok) {
                      throw new Error("Error al obtener las skills del usuario");
                    }
                    return respSkill.json();
                  })
                  .then((skillsData) => {
                    return {
                      ...user,
                      role: roleText.trim(), // Rol devuelto (por ejemplo "manager" o "developer")
                      skills: Array.isArray(skillsData) ? skillsData : [] // Nos aseguramos que sea un arreglo
                    };
                  })
              )
          )
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

  // Función para editar una skill
  const handleEditSkill = (userId, skill) => {
    // Se solicitan nuevos valores por prompt
    const newName = window.prompt("Ingrese el nuevo nombre de la skill:", skill.name);
    const newDescription = window.prompt("Ingrese la nueva descripción de la skill:", skill.description);
    const payload = {};
    if (newName && newName !== skill.name) {
      payload.name = newName;
    }
    if (newDescription && newDescription !== skill.description) {
      payload.description = newDescription;
    }
    if (Object.keys(payload).length === 0) {
      alert("No se realizaron cambios.");
      return;
    }
    // Enviamos la petición PATCH para actualizar la skill
    fetch(`http://localhost:8081/api/skills/${skill.idSkills}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Error al actualizar la skill");
        }
        return response.json();
      })
      .then((updatedSkill) => {
        // Actualizamos la lista de usuarios en el estado, reemplazando la skill actualizada
        setUsers((prevUsers) =>
          prevUsers.map((u) => {
            if (u.idUser === userId) {
              return {
                ...u,
                skills: u.skills.map((s) =>
                  s.idSkills === skill.idSkills ? { ...s, ...updatedSkill } : s
                )
              };
            }
            return u;
          })
        );
      })
      .catch((err) => {
        alert(err.message);
      });
  };

  if (loading) {
    return <p className="text-center mt-8">Cargando usuarios...</p>;
  }
  if (error) {
    return <p className="text-center mt-8 text-red-500">{error}</p>;
  }

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4">Lista de Usuarios</h1>
      <table className="min-w-full bg-black bg-opacity-20">
        <thead>
          <tr>
            <th className="px-4 py-2 text-left">ID</th>
            <th className="px-4 py-2 text-left">Nombre</th>
            <th className="px-4 py-2 text-left">Rol</th>
            <th className="px-4 py-2 text-left">Skills</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.idUser} className="border-b border-gray-600">
              <td className="px-4 py-2">{u.idUser}</td>
              <td className="px-4 py-2">{u.name}</td>
              <td className="px-4 py-2">{u.role}</td>
              <td className="px-4 py-2">
                {u.skills && u.skills.length > 0 ? (
                  u.skills.map((skill) => (
                    <div key={skill.idSkills} className="mb-2">
                      <div>
                        <span className="font-bold">Skill:</span> {skill.name}
                        <br />
                        <span className="font-bold">Descripción:</span> {skill.description}
                      </div>
                      <button
                        onClick={() => handleEditSkill(u.idUser, skill)}
                        className="mt-1 rounded border border-gray-300 px-2 py-1 hover:bg-gray-100"
                      >
                        Editar
                      </button>
                    </div>
                  ))
                ) : (
                  <span>No hay skills</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default UsersList;
