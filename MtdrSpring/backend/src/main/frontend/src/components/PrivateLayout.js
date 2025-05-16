// src/components/PrivateLayout.js
import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import Navbar from "./Navbar";

function PrivateLayout({ user, onLogout }) {
  if (!user) {
    // si no hay user logueado, redirigir a "/"
    return <Navigate to="/" />;
  }

  return (
    <div className="min-h-screen  border-black border-solid bg-[#212233] text-gray-900 text-gray-100">
      <Navbar user={user} onLogout={onLogout} />
      {/* El contenido de las rutas hijas se renderiza aquí */}
      <Outlet />
    </div>
  );
}

export default PrivateLayout;
