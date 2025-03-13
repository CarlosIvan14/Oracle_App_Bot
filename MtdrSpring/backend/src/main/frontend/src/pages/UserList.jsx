// src/pages/UserList.js
import React, { useEffect, useState } from 'react';
import { FaUser, FaEdit, FaSave } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import NewUserModal from '../components/NewUserModal/NewUserModal'; // Ajusta la ruta si es distinto

function UserList() {
  const [users, setUsers] = useState([]);
  const [editingUserId, setEditingUserId] = useState(null);
  const [editedSkills, setEditedSkills] = useState("");

  // Controlar la apertura/cierre del modal de nuevo usuario
  const [showUserModal, setShowUserModal] = useState(false);

  const navigate = useNavigate();

  // Carga inicial de todos los usuarios
  useEffect(() => {
    fetch('/users') // Ajusta la URL si tu backend corre en otro puerto o ruta (e.g. http://localhost:8080/users)
      .then(res => res.json())
      .then(data => setUsers(data))
      .catch(err => console.error(err));
  }, []);

  // Inicia edición de un usuario
  const handleEditClick = (userId, currentSkills) => {
    setEditingUserId(userId);
    setEditedSkills(currentSkills || "");
  };

  // Guardar cambios en skills vía PATCH
  const handleSaveClick = async (userId) => {
    try {
      const response = await fetch(`/users/${userId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ skill: editedSkills })
      });
      if (!response.ok) {
        throw new Error('Error al actualizar skills');
      }
      const updatedUser = await response.json();

      // Actualizamos la lista local de usuarios
      setUsers(prev =>
        prev.map(u => (u.idUser === userId ? updatedUser : u))
      );

      // Cerramos modo edición
      setEditingUserId(null);
      setEditedSkills("");
    } catch (error) {
      console.error(error);
    }
  };

  // Abrir/Cerrar modal de Nuevo Usuario
  const handleOpenUserModal = () => {
    setShowUserModal(true);
  };
  const handleCloseUserModal = () => {
    setShowUserModal(false);
    // Opcional: recargar la lista si quieres ver el nuevo usuario de inmediato
    reloadUsers();
  };

  // (Opcional) función para recargar la lista de usuarios
  const reloadUsers = () => {
    fetch('/users')
      .then(res => res.json())
      .then(data => setUsers(data))
      .catch(err => console.error(err));
  };

  // (Opcional) callback si el modal te permite saber cuándo se creó un usuario
  // para actualizar la lista sin recargar todo.
  const handleUserCreated = (newUser) => {
    // Insertamos el nuevo usuario en la lista
    setUsers([...users, newUser]);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-700 to-gray-950 p-8">
      <div className="flex items-center justify-center mb-6">
        <NewUserModal
            isRegistering={true}
            isOpen={showUserModal}
            onClose={handleCloseUserModal}
        />
       <button
            onClick={() => navigate('/')}
            className="
              bg-transparent
              text-white
              font-semibold
              py-2
              px-4
              rounded-full
              transition
              duration-200
              transform hover:scale-105
              hover:border hover:border-red-500
            "
          >
            Volver al Dashboard
          </button>
          </div>
      {/* Encabezado con botones */}
      <div className="flex items-center justify-center mb-6">
        <h1 className="text-3xl text-white font-bold">Lista de Usuarios</h1>
      </div>

      {/* Tabla de usuarios */}
      <div className="max-w-5xl mx-auto bg-black bg-opacity-40 p-6 rounded-xl">
        <table className="min-w-full table-auto text-white">
          <thead>
            <tr className="border-b border-gray-600">
              <th className="py-3 px-4 text-left">Usuario</th>
              <th className="py-3 px-4 text-left">Nombre</th>
              <th className="py-3 px-4 text-left">Skills</th>
              <th className="py-3 px-4 text-left">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr
                key={u.idUser}
                className="border-b border-gray-600 hover:bg-gray-700"
              >
                {/* Ícono de usuario */}
                <td className="py-3 px-4">
                  <FaUser className="text-2xl" />
                </td>

                {/* Nombre del usuario */}
                <td className="py-3 px-4">
                  {u.name}
                </td>
                <td className="py-3 px-4">
                  {editingUserId === u.idUser ? (
                    <input
                      type="text"
                      className="bg-gray-800 p-2 rounded w-full text-white"
                      value={editedSkills}
                      onChange={(e) => setEditedSkills(e.target.value)}
                    />
                  ) : (
                    u.skill || "Sin skills"
                  )}
                </td>
                <td className="py-3 px-4">
                  {editingUserId === u.idUser ? (
                    <button
                      onClick={() => handleSaveClick(u.idUser)}
                      className="
                        inline-flex items-center
                        bg-green-600
                        text-white
                        py-2 px-3
                        rounded-full
                        hover:bg-green-700
                        transition
                      "
                    >
                      <FaSave className="mr-1" />
                      Guardar
                    </button>
                  ) : (
                    <button
                      onClick={() => handleEditClick(u.idUser, u.skill)}
                      className="
                        inline-flex items-center
                        bg-transparent
                        text-blue-400
                        py-2 px-3
                        rounded-full
                        border
                        border-blue-400
                        hover:text-white
                        hover:bg-blue-500
                        transition
                      "
                    >
                      <FaEdit className="mr-1" />
                      Editar
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default UserList;
