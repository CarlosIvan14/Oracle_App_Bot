// src/routes/Profile.js
import React from "react";
import { FaUser } from "react-icons/fa";

function Profile({ user }) {
  if (!user) {
    return (
      <div className="p-6">
        <p>No user logged in</p>
      </div>
    );
  }

  // Si tu modelo se llama "skill" o "skills", ajusta aquí:
  const userSkills = user.skill || "No skills provided";

  return (
    <div
      className="
        min-h-screen
        flex flex-col items-center justify-center
        p-4
        text-gray-100
      "
    >
      {/* Encabezado */}
      <h1 className="text-4xl font-bold mb-2">Perfil de {user.name}</h1>
      <p className="mb-6 text-gray-400">
        {/* Subtítulo o descripción */}
        I'm a creative web developer
      </p>

      {/* Contenedor principal (dos columnas en pantallas medianas) */}
      <div className="flex flex-col md:flex-row gap-8 w-full max-w-4xl">
        {/* Columna izquierda: Ícono + "Acerca de mí" */}
        <div className="flex-1 flex flex-col items-center bg-black bg-opacity-50 p-6 rounded-lg">
          {/* Ícono de usuario en círculo */}
          <div className="bg-[#212233] p-4 rounded-full mb-4">
            <FaUser className="text-6xl text-gray-400" />
          </div>
          <h2 className="text-xl font-semibold mb-2">About me</h2>
          <p className="text-center text-gray-300">{userSkills}</p>
        </div>

        {/* Columna derecha: Detalles */}
        <div className="flex-1 flex flex-col justify-center bg-black bg-opacity-50 p-6 rounded-lg">
          <h2 className="text-xl font-semibold mb-4">Details</h2>
          <p className="mb-2">
            <span className="font-bold">Name: </span>
            {user.name}
          </p>
          <p className="mb-2">
            <span className="font-bold">Role: </span>
            {user.role}
          </p>
          <p className="mb-2">
            <span className="font-bold">ID: </span>
            {user.idUser}
          </p>
          {/* Si tu backend tuviera más datos (edad, ubicación, etc.), agrégalos aquí */}
        </div>
      </div>
    </div>
  );
}

export default Profile;
