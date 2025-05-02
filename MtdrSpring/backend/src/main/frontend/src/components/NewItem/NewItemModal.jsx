import React, { useState } from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

function NewItemModal({ addItem, isInserting }) {
  // Estados para el modal
  const [showModal, setShowModal] = useState(false);

  // Estados para los campos del formulario
  const [description, setDescription] = useState("");
  const [deadline, setDeadline] = useState(null); // objeto Date
  const [priority, setPriority] = useState("");
  const [userPosition, setUserPosition] = useState("");

  // Estado para la lista de usuarios retornada por AI y para el indicador de carga
  const [teamMembers, setTeamMembers] = useState([]);
  const [isFetching, setIsFetching] = useState(false);

  // Función para llamar al endpoint de OpenAI y obtener la lista ordenada de usuarios
  const assignByAi = () => {
    if (!description) {
      alert("Primero ingresa la descripción de la tarea");
      return;
    }
    setIsFetching(true);
    fetch("http://140.84.170.68/assignment/by-ai", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description })
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Error al asignar por AI");
        }
        return response.json();
      })
      .then((data) => {
        // Se espera que data sea un arreglo de usuarios ordenados, con al menos el campo "idUser"
        setTeamMembers(data);
        setIsFetching(false);
      })
      .catch((error) => {
        console.error(error);
        alert("Error al obtener asignación AI");
        setIsFetching(false);
      });
  };

  // Manejo de submit del formulario
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!description || !deadline || !priority || !userPosition) {
      alert("Por favor, completa todos los campos");
      return;
    }
    if (teamMembers.length === 0) {
      alert(
        "Debes obtener la lista de usuarios mediante 'Assign By Ai' antes de asignar la tarea",
      );
      return;
    }
    const pos = parseInt(userPosition, 10) - 1;
    if (pos < 0 || pos >= teamMembers.length) {
      alert(`La posición debe ser entre 1 y ${teamMembers.length}`);
      return;
    }
    // Se utiliza el idUser del usuario seleccionado
    const assignedUserId = teamMembers[pos].idUser;
    const isoDate = deadline.toISOString().split("T")[0];
    const newTask = {
      description,
      deadline: isoDate,
      priority: parseInt(priority, 10),
      assignedUser: { idUser: assignedUserId },
    };
    addItem(newTask);
    // Limpiar campos y cerrar modal
    setDescription("");
    setDeadline(null);
    setPriority("");
    setUserPosition("");
    setTeamMembers([]);
    setShowModal(false);
  };

  // Icono de lápiz (SVG)
  const PencilIcon = (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      className="h-5 w-5 mr-1"
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
      strokeWidth={2}
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M15.232 5.232l3.536 3.536m-2.036-5.036l-8.5 8.5a2.121 2.121 0 000 3l2 2a2.121 2.121 0 003 0l8.5-8.5a2.121 2.121 0 000-3l-2-2a2.121 2.121 0 00-3 0z"
      />
    </svg>
  );

  return (
    <>
      {/* Botón para abrir el modal */}
      <button
        className="flex items-center bg-transparent text-white font-semibold py-2 px-4 rounded-full transition duration-200 transform hover:scale-105 hover:border hover:border-cyan-500"
        onClick={() => setShowModal(true)}
      >
        {PencilIcon}
        Añadir Tarea
      </button>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-gray-800 text-white w-full max-w-md mx-auto p-6 rounded-lg shadow-lg relative transition duration-200 transform hover:scale-105">
            {/* Botón para cerrar el modal */}
            <button
              className="absolute top-3 right-3 text-gray-400 hover:text-white transition duration-200 transform hover:scale-105 hover:border hover:border-cyan-500"
              onClick={() => setShowModal(false)}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>

            <h2 className="text-2xl font-bold mb-4 text-center">Nueva Tarea</h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block mb-1 font-semibold">Descripción</label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Descripción"
                  className="w-full bg-gray-700 text-white px-3 py-2 rounded-3xl focus:outline-none focus:ring-2 focus:ring-cyan-500 transition duration-200 transform hover:scale-105"
                />
              </div>

              <div>
                <label className="block mb-1 font-semibold">Fecha Límite</label>
                <DatePicker
                  selected={deadline}
                  onChange={(date) => setDeadline(date)}
                  dateFormat="yyyy-MM-dd"
                  placeholderText="Selecciona una fecha"
                  className="w-full bg-gray-700 text-white px-3 py-2 rounded-3xl focus:outline-none focus:ring-2 focus:ring-cyan-500 transition duration-200 transform hover:scale-105"
                  wrapperClassName="w-full"
                  calendarClassName="bg-gray-800 text-white border border-gray-600"
                />
              </div>

              <div>
                <label className="block mb-1 font-semibold">Prioridad</label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value)}
                  className="w-full bg-gray-700 text-white px-3 py-2 rounded-3xl focus:outline-none focus:ring-2 focus:ring-cyan-500 transition duration-200 transform hover:scale-105"
                >
                  <option value="">Selecciona prioridad</option>
                  <option value="1">Alta</option>
                  <option value="2">Media</option>
                  <option value="3">Baja</option>
                </select>
              </div>

              {/* Botón para llamar a "Assign By Ai" */}
              <div className="flex justify-center">
                {isFetching ? (
                  <div className="flex items-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-300"></div>
                    <span className="ml-2">Asignando...</span>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={assignByAi}
                    className="bg-transparent text-white font-semibold py-2 px-4 rounded-full transition duration-200 transform hover:scale-105 hover:border hover:border-cyan-500"
                  >
                    Assign By Ai
                  </button>
                )}
              </div>

              {/* Mostrar la lista de usuarios obtenida por AI */}
              {teamMembers.length > 0 && (
                <div>
                  <label className="block mb-1 font-semibold">
                    Rank de Usuarios para esta tarea:
                  </label>
                  <div className="max-h-40 overflow-y-auto border border-gray-600 p-2 rounded">
                    <ul className="ml-4 mb-2 text-sm">
                      {teamMembers.map((member, index) => (
                        <li key={member.idUser}>
                          {index + 1}. {member.name} (ID: {member.idUser})
                        </li>
                      ))}
                    </ul>
                  </div>
                  <label className="block mb-1 font-semibold">
                    Ingresa la posición del usuario al que quieres asignar la
                    tarea (1 - {teamMembers.length})
                  </label>
                  <input
                    type="number"
                    value={userPosition}
                    onChange={(e) => setUserPosition(e.target.value)}
                    placeholder="Número de posición"
                    className="w-full bg-gray-700 text-white px-3 py-2 rounded-3xl focus:outline-none focus:ring-2 focus:ring-cyan-500 transition duration-200 transform hover:scale-105"
                  />
                </div>
              )}

              <button
                type="submit"
                disabled={isInserting}
                className="w-full bg-transparent text-white font-semibold py-2 rounded-3xl transition duration-200 transform hover:scale-105 hover:border hover:border-cyan-500"
              >
                {isInserting ? "Agregando..." : "Agregar Tarea +"}
              </button>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

export default NewItemModal;
