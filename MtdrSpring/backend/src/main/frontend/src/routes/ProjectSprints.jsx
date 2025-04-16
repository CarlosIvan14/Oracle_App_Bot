// src/routes/ProjectSprints.js
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

function ProjectSprints() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [sprints, setSprints] = useState([]);
  const [projectUser, setProjectUser] = useState(null);
  const [loadingSprints, setLoadingSprints] = useState(true);
  const [loadingUser, setLoadingUser] = useState(true);
  const [error, setError] = useState('');

  // Obtenemos el usuario "Oracle" logueado almacenado en localStorage
  const currentUser = JSON.parse(localStorage.getItem('user') || 'null');

  // Fetch de los sprints del proyecto
  useEffect(() => {
    fetch(`http://localhost:8081/api/sprints/project/${projectId}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Error al obtener sprints');
        }
        return response.json();
      })
      .then((data) => {
        setSprints(data);
        setLoadingSprints(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoadingSprints(false);
      });
  }, [projectId]);

  // Fetch para obtener el rol del usuario en el proyecto usando el endpoint que devuelve un texto plano
  useEffect(() => {
    if (!currentUser) {
      setError('Usuario no logueado');
      setLoadingUser(false);
      return;
    }
    fetch(`http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${currentUser.idUser}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Error al obtener la información del rol en el proyecto');
        }
        return response.text(); // La respuesta es un texto plano ("manager" o "developer")
      })
      .then((roleText) => {
        setProjectUser({ roleUser: roleText });
        setLoadingUser(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoadingUser(false);
      });
  }, [currentUser, projectId]);

  // Función para actualizar el estado (habilitar/deshabilitar) de un sprint
  const handleToggleSprint = async (sprint) => {
    const newStatus = sprint.description === "Active" ? "idle" : "Active";
    try {
      const response = await fetch(`http://localhost:8081/api/sprints/${sprint.id_sprint}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ description: newStatus })
      });
      if (!response.ok) {
        throw new Error('Error al actualizar el sprint');
      }
      // Actualizamos el estado local sin hacer un nuevo fetch
      const updatedSprints = sprints.map((sp) =>
        sp.id_sprint === sprint.id_sprint ? { ...sp, description: newStatus } : sp
      );
      setSprints(updatedSprints);
    } catch (err) {
      console.error(err.message);
      alert(err.message);
    }
  };

  if (loadingSprints || loadingUser) {
    return <p className="text-center mt-8">Cargando datos...</p>;
  }

  if (error) {
    return <p className="text-center mt-8 text-red-500">{error}</p>;
  }

  // Filtramos los sprints: si el projectUser es developer y el sprint está en estado "idle", no se muestra.
  const filteredSprints = sprints.filter(sprint => {
    if (projectUser && projectUser.roleUser === 'developer' && sprint.description.toLowerCase() === 'idle') {
      return false;
    }
    return true;
  });

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4 text-center">Sprints del Proyecto {projectId}</h1>

      {/* Botones adicionales para managers en el centro, sin fondo y redondeados */}
      {projectUser && projectUser.roleUser === 'manager' && (
        <div className="flex justify-center space-x-4 mb-4">
          <button
            onClick={() => navigate(`/projects/${projectId}/users`)}
            className="rounded-full border border-gray-300 px-4 py-2 hover:bg-gray-100"
          >
            Ver Usuarios
          </button>

          <button
            onClick={() => navigate(`/projects/${projectId}/sprints/create`)}
            className="rounded-full border border-gray-300 px-4 py-2 hover:bg-gray-100"
          >
            Crear Sprints
          </button>
        </div>
      )}

      <div className="grid grid-cols-4 gap-4">
        {filteredSprints.length > 0 ? (
          filteredSprints.map((sprint) => (
            <div
              key={sprint.id_sprint}
              className="bg-black bg-opacity-20 p-4 rounded-2xl hover:bg-opacity-30 cursor-pointer"
              onClick={() => navigate(`/projects/${projectId}/sprint/${sprint.id_sprint}/tasks`)}
            >
              <h2 className="text-xl font-semibold">{sprint.name}</h2>
              <p>Status: {sprint.description}</p>
              {/* Botón para managers: redondeado con fondo rojo y que no propaga el click */}
              {projectUser && projectUser.roleUser === 'manager' && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleToggleSprint(sprint);
                  }}
                  className="mt-2 rounded-full bg-red-600 hover:bg-red-700 text-white font-bold py-1 px-2 aling-right"
                >
                  {sprint.description === "Active" ? "Deshabilitar" : "Habilitar"}
                </button>
              )}
            </div>
          ))
        ) : (
          <p className="col-span-4 text-gray-400 text-center">No hay sprints.</p>
        )}
      </div>
    </div>
  );
}

export default ProjectSprints;
