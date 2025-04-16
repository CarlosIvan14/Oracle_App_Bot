// src/routes/ProjectSprints.js
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

/** Íconos de ejemplo. Reemplázalos con los de tu librería preferida */
const IconUsers = () => <span>👥</span>;
const IconPlus = () => <span>＋</span>;
const IconBack = () => <span>←</span>;
const IconCancel = () => <span>✕</span>;

function ProjectSprints() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [sprints, setSprints] = useState([]);
  const [projectUser, setProjectUser] = useState(null);
  const [loadingSprints, setLoadingSprints] = useState(true);
  const [loadingUser, setLoadingUser] = useState(true);
  const [error, setError] = useState('');
  
  // Obtenemos el usuario "Oracle" logueado (guardado en localStorage)
  const currentUser = JSON.parse(localStorage.getItem('user') || 'null');
  
  // Estados para el modal de crear sprint
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newSprintName, setNewSprintName] = useState('');
  const [newSprintDate, setNewSprintDate] = useState('');
  const [newSprintDescription, setNewSprintDescription] = useState('Active');
  
  // Fetch de los sprints del proyecto
  useEffect(() => {
    fetch(`http://localhost:8081/api/sprints/project/${projectId}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Error al obtener sprints');
        }
        return response.json();
      })
      .then(data => {
        setSprints(data);
        setLoadingSprints(false);
      })
      .catch(err => {
        setError(err.message);
        setLoadingSprints(false);
      });
  }, [projectId]);

  // Fetch para obtener el rol del usuario en el proyecto (devuelve texto plano: "manager" o "developer")
  useEffect(() => {
    if (!currentUser) {
      setError('Usuario no logueado');
      setLoadingUser(false);
      return;
    }
    fetch(`http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${currentUser.idUser}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Error al obtener la información del rol en el proyecto');
        }
        return response.text();
      })
      .then(roleText => {
        setProjectUser({ roleUser: roleText });
        setLoadingUser(false);
      })
      .catch(err => {
        setError(err.message);
        setLoadingUser(false);
      });
  }, [currentUser, projectId]);
  
  // Función para cambiar el estado de un sprint (toggle Active/idle)
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
      // Actualizamos la lista localmente
      const updatedSprints = sprints.map(sp =>
        sp.id_sprint === sprint.id_sprint ? { ...sp, description: newStatus } : sp
      );
      setSprints(updatedSprints);
    } catch (err) {
      console.error(err.message);
      alert(err.message);
    }
  };

  // Función para abrir el modal de crear sprint
  const openCreateModal = () => {
    setShowCreateModal(true);
  };
  
  const closeCreateModal = () => {
    setShowCreateModal(false);
    setNewSprintName('');
    setNewSprintDate('');
    setNewSprintDescription('Active');
  };
  
  // Función para crear un nuevo sprint usando POST
  const handleCreateSprint = async () => {
    if (!newSprintName || !newSprintDate) {
      alert("Completa el nombre y la fecha de creación");
      return;
    }
    const payload = {
      creation_ts: newSprintDate,
      description: newSprintDescription,
      name: newSprintName,
      project: { id_project: parseInt(projectId, 10) }
    };
    try {
      const response = await fetch(`http://localhost:8081/api/sprints`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        throw new Error("Error al crear el sprint");
      }
      const createdSprint = await response.json();
      setSprints([...sprints, createdSprint]);
      closeCreateModal();
    } catch (err) {
      alert(err.message);
    }
  };

  // Botón para regresar a la vista de sprints
  const handleGoBack = () => {
    navigate(`/home`);
  };

  if (loadingSprints || loadingUser) {
    return <p className="text-center mt-8">Cargando datos...</p>;
  }
  if (error) {
    return <p className="text-center mt-8 text-red-500">{error}</p>;
  }

  // Filtramos los sprints: si el usuario es developer y el sprint está en estado "idle", no se muestra.
  const filteredSprints = sprints.filter(sprint => {
    if (projectUser && projectUser.roleUser === 'developer' && sprint.description.toLowerCase() === 'idle') {
      return false;
    }
    return true;
  });

  return (
    <div className="p-6">
      {/* Botón Go Back */}
      <button
        onClick={handleGoBack}
        className="mb-4 rounded-full border border-gray-300 px-3 py-1 hover:bg-gray-100 dark:hover:bg-customDarkligth"
      >
        <IconBack /> Volver a la lista de Proyectos
      </button>

      <h1 className="text-3xl font-bold mb-4 text-center">Sprints del Proyecto {projectId}</h1>

      {/* Botones adicionales para managers */}
      {projectUser && projectUser.roleUser === 'manager' && (
        <div className="flex justify-center space-x-4 mb-4">
          <button
            onClick={() => navigate(`/projects/${projectId}/users`)}
            className="rounded-full border border-gray-300 px-4 py-2 hover:bg-gray-100 dark:hover:bg-customDarkligth"
          >
            <IconUsers /> Ver Usuarios
          </button>
          <button
            onClick={openCreateModal}
            className="rounded-full border border-gray-300 px-4 py-2 hover:bg-gray-100 dark:hover:bg-customDarkligth"
          >
            <IconPlus /> Crear Sprint
          </button>
        </div>
      )}

      {/* Lista de sprints */}
      <div className="grid grid-cols-4 gap-4">
        {filteredSprints.length > 0 ? (
          filteredSprints.map((sprint) => (
            <div
              key={sprint.id_sprint}
              className="bg-black bg-opacity-20 p-4 rounded-2xl hover:bg-opacity-30 cursor-pointer"
              onClick={() => navigate(`/projects/${projectId}/sprint/${sprint.id_sprint}/tasks`)}
            >
              <div className="flex justify-between items-center">
                <div>
                  <h2 className="text-xl font-semibold">{sprint.name}</h2>
                  <p>
                    Estado: {sprint.description === "Active" ? "🟢" : "🔴"}
                  </p>
                </div>
                {projectUser && projectUser.roleUser === 'manager' && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleToggleSprint(sprint);
                    }}
                    className="rounded-full bg-red-600 hover:bg-red-700 text-white font-bold py-1 px-2"
                  >
                    {sprint.description === "Active" ? "Deshabilitar" : "Habilitar"}
                  </button>
                )}
              </div>
            </div>
          ))
        ) : (
          <p className="col-span-4 text-gray-400 text-center">No hay sprints.</p>
        )}
      </div>

      {/* Modal para crear sprint */}
      {showCreateModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-70 dark:bg-black dark:bg-opacity-70"
          onClick={closeCreateModal}
        >
          <div
            className="max-w-md w-full p-6 bg-white dark:bg-customDark dark:text-white rounded-2xl relative"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="text-xl font-bold mb-4">Crear Sprint</h2>
            <div className="space-y-4">
              <div>
                <label className="block mb-1">Nombre</label>
                <input
                  type="text"
                  value={newSprintName}
                  onChange={(e) => setNewSprintName(e.target.value)}
                  className="w-full rounded-md p-2 border border-gray-300 dark:border-gray-600 dark:bg-customDarkligth dark:text-white"
                  placeholder="Nombre del sprint"
                />
              </div>
              <div>
                <label className="block mb-1">Fecha de Creación</label>
                <input
                  type="date"
                  value={newSprintDate}
                  onChange={(e) => setNewSprintDate(e.target.value)}
                  className="w-full rounded-md p-2 border border-gray-300 dark:border-gray-600 dark:bg-customDarkligth dark:text-white"
                />
              </div>
              <div>
                <label className="block mb-1">Descripción</label>
                <input
                  type="text"
                  value={newSprintDescription}
                  onChange={(e) => setNewSprintDescription(e.target.value)}
                  className="w-full rounded-md p-2 border border-gray-300 dark:border-gray-600 dark:bg-customDarkligth dark:text-white"
                  placeholder="Estado (por ejemplo: Active)"
                />
              </div>
            </div>
            <div className="mt-4 flex justify-end space-x-4">
              <button
                onClick={closeCreateModal}
                className="rounded-full border border-gray-300 px-4 py-2 hover:bg-gray-100 dark:hover:bg-customDarkligth"
              >
                <IconCancel /> Cancelar
              </button>
              <button
                onClick={handleCreateSprint}
                className="rounded-full border border-green-500 text-green-500 px-4 py-2 hover:bg-green-500 hover:text-white"
              >
                <IconPlus /> Crear
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ProjectSprints;
