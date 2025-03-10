import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css'; // Import base CSS

function NewItemModal({ addItem, isInserting }) {
  // Estados para el modal
  const [showModal, setShowModal] = useState(false);

  // Estados para los campos del formulario
  const [description, setDescription] = useState('');
  const [deadline, setDeadline] = useState(null); // almacenaremos un objeto Date
  const [priority, setPriority] = useState('');
  const [assignedUserId, setAssignedUserId] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!description || !deadline || !priority || !assignedUserId) {
      alert("Por favor, completa todos los campos");
      return;
    }

    // Convertir la fecha a formato yyyy-MM-dd (o como necesites)
    const isoDate = deadline.toISOString().split('T')[0];

    const newTask = {
      description,
      deadline: isoDate, // Usamos la fecha formateada
      priority: parseInt(priority, 10),
      assignedUser: { id: parseInt(assignedUserId, 10) }
    };
    addItem(newTask);

    // Limpiar campos
    setDescription('');
    setDeadline(null);
    setPriority('');
    setAssignedUserId('');

    // Cerrar modal
    setShowModal(false);
  };

  // Icono de papel con lápiz (SVG)
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
        d="M15.232 5.232l3.536 3.536m-2.036-5.036l-8.5 8.5a2.121 
           2.121 0 000 3l2 2a2.121 2.121 0 003 0l8.5-8.5a2.121 2.121 0 
           000-3l-2-2a2.121 2.121 0 00-3 0z"
      />
    </svg>
  );

  return (
    <>
      {/* Botón que abre el modal */}
      <button
        className="
          flex items-center 
          bg-transparent
          hover:border-cyan-500 
          text-white 
          font-semibold 
          py-2 
          px-4 
          rounded-full
          transition 
          duration-200
          transform hover:scale-105
        "
        onClick={() => setShowModal(true)}
      >
        {PencilIcon}
        Añadir Tarea
      </button>

      {/* Modal */}
      {showModal && (
        <div
          className="
            fixed 
            inset-0 
            flex 
            items-center 
            justify-center 
            bg-black 
            bg-opacity-50 
            z-50
          "
        >
          <div
            className="
              bg-gray-800 
              text-white 
              w-full 
              max-w-md 
              mx-auto 
              p-6 
              rounded-lg 
              shadow-lg 
              relative
              transition duration-200 transform hover:scale-105
            "
          >
            {/* Botón para cerrar modal */}
            <button
              className="
                absolute 
                top-3 
                right-3 
                text-gray-400 
                hover:text-white 
                transition
                duration-200
                transform hover:scale-105
              "
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

            <h2 className="text-2xl font-bold mb-4 text-center">
              Nueva Tarea
            </h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block mb-1 font-semibold">
                  Descripción
                </label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Descripción"
                  className="
                    w-full 
                    bg-gray-700 
                    text-white 
                    px-3 
                    py-2 
                    rounded-3xl 
                    focus:outline-none 
                    focus:ring-2 
                    focus:ring-cyan-500
                    transition duration-200 transform hover:scale-105
                  "
                />
              </div>

              <div>
                <label className="block mb-1 font-semibold">
                  Fecha Límite
                </label>
                {/* DatePicker personalizado */}
                <DatePicker
                  selected={deadline}
                  onChange={(date) => setDeadline(date)}
                  dateFormat="yyyy-MM-dd" // El formato que verás en el input
                  placeholderText="Selecciona una fecha"
                  className="
                    w-full
                    bg-gray-700 
                    text-white 
                    px-3 
                    py-2 
                    rounded-3xl 
                    focus:outline-none 
                    focus:ring-2 
                    focus:ring-cyan-500
                    transition duration-200 transform hover:scale-105
                  "
                  // Para que el calendario emergente sea oscuro
                  calendarClassName="bg-gray-800 text-white border border-gray-600"
                />
              </div>

              <div>
                <label className="block mb-1 font-semibold">
                  Prioridad
                </label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value)}
                  className="
                    w-full 
                    bg-gray-700 
                    text-white 
                    px-3 
                    py-2 
                    rounded-3xl 
                    focus:outline-none 
                    focus:ring-2 
                    focus:ring-cyan-500
                    transition duration-200 transform hover:scale-105
                  "
                >
                  <option value="">Selecciona prioridad</option>
                  <option value="1">Alta</option>
                  <option value="2">Media</option>
                  <option value="3">Baja</option>
                </select>
              </div>

              <div>
                <label className="block mb-1 font-semibold">
                  ID Usuario
                </label>
                <input
                  type="number"
                  value={assignedUserId}
                  onChange={(e) => setAssignedUserId(e.target.value)}
                  placeholder="ID del usuario"
                  className="
                    w-full 
                    bg-gray-700 
                    text-white 
                    px-3 
                    py-2 
                    rounded-3xl
                    focus:outline-none 
                    focus:ring-2 
                    focus:ring-cyan-500
                    transition duration-200 transform hover:scale-105
                  "
                />
              </div>

              <button
                type="submit"
                disabled={isInserting}
                className="
                  w-full
                  bg-cyan-600 
                  hover:bg-cyan-700 
                  text-white 
                  font-semibold 
                  py-2 
                  rounded-3xl 
                  transition 
                  duration-200
                  transform hover:scale-105
                "
              >
                {isInserting ? "Agregando..." : "Agregar Tarea"}
              </button>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

export default NewItemModal;
