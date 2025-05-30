import React, { useState } from "react";
import config from "../../config";

function NewUserModal({ isOpen, onClose, isRegistering }) {
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [skill, setSkill] = useState("");
  const [telegramId, setTelegramId] = useState("");
  const [telegramUsername, setTelegramUsername] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name || !password || !skill || !telegramId || !telegramUsername) {
      alert("Por favor, completa todos los campos");
      return;
    }

    const newUser = {
      name,
      password,
      role: "developer", // valor fijo
      skill,
      telegramId,
      telegramUsername,
    };

    fetch(`${config.apiBaseUrl}/users/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newUser),
    })
      .then((response) => {
        if (response.ok) {
          return response.json();
        }
        throw new Error("Error al registrar usuario");
      })
      .then((result) => {
        // Reiniciar campos y cerrar modal
        setName("");
        setPassword("");
        setSkill("");
        setTelegramId("");
        setTelegramUsername("");
        onClose(); // Cerrar el modal después de registrar
      })
      .catch((error) => {
        alert(error.message);
      });
  };

  if (!isOpen) return null; // No renderizar el modal si no está abierto

  return (
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
            hover:border hover:border-purple-500
          "
          onClick={onClose}
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
          Registrar Usuario
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Campos del formulario */}
          <div>
            <label className="block mb-1 font-semibold">Nombre</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Nombre"
              className="
                w-full 
                bg-gray-700 
                text-white 
                px-3 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-purple-500
                transition duration-200 transform hover:scale-105
              "
            />
          </div>

          <div>
            <label className="block mb-1 font-semibold">Contraseña</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Contraseña"
              className="
                w-full 
                bg-gray-700 
                text-white 
                px-3 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-purple-500
                transition duration-200 transform hover:scale-105
              "
            />
          </div>

          <div>
            <label className="block mb-1 font-semibold">Skill</label>
            <input
              type="text"
              value={skill}
              onChange={(e) => setSkill(e.target.value)}
              placeholder="Skill"
              className="
                w-full 
                bg-gray-700 
                text-white 
                px-3 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-purple-500
                transition duration-200 transform hover:scale-105
              "
            />
          </div>

          <div>
            <label className="block mb-1 font-semibold">Telegram ID</label>
            <input
              type="text"
              value={telegramId}
              onChange={(e) => setTelegramId(e.target.value)}
              placeholder="Telegram ID"
              className="
                w-full 
                bg-gray-700 
                text-white 
                px-3 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-purple-500
                transition duration-200 transform hover:scale-105
              "
            />
          </div>

          <div>
            <label className="block mb-1 font-semibold">
              Telegram Username
            </label>
            <input
              type="text"
              value={telegramUsername}
              onChange={(e) => setTelegramUsername(e.target.value)}
              placeholder="Telegram Username"
              className="
                w-full 
                bg-gray-700 
                text-white 
                px-3 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-purple-500
                transition duration-200 transform hover:scale-105
              "
            />
          </div>

          <button
            type="submit"
            disabled={isRegistering}
            className="
              w-full
              bg-transparent
              text-white 
              font-semibold 
              py-2 
              rounded-3xl 
              transition 
              duration-200
              transform hover:scale-105
              hover:border hover:border-purple-500
              flex items-center justify-center
            "
          >
            {isRegistering ? (
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white"></div>
            ) : (
              "Registrar Usuario"
            )}
          </button>
        </form>
      </div>
    </div>
  );
}

export default NewUserModal;
