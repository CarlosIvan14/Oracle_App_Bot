// src/components/Navbar.js
import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ThemeContext } from '../context/ThemeContext';
import ThemeToggle from './ThemeToggle';

function Navbar({ user, onLogout }) {
  const navigate = useNavigate();

  return (
    <nav
      className="
        w-full py-4 px-6 flex justify-between items-center
        bg-red-800 dark:bg-red-900
        text-gray-100 dark:text-gray-100
        shadow
      "
    >
      <div className="font-bold text-xl cursor-pointer">
        <Link to="/home">Oracle Projects</Link>
      </div>

      <div className="flex items-center space-x-4">
        {/* Link a Home */}
        <Link to="/home" className="hover:underline">
          Home
        </Link>

        {/* Si es manager, ver Users y Reports */}
        {user?.role === 'manager' && (
          <>
            <Link to="/users" className="hover:underline">
              Ver Usuarios
            </Link>
            <Link to="/reports" className="hover:underline">
              Reportes
            </Link>
          </>
        )}

        {/* Profile */}
        <Link to="/profile" className="hover:underline">
          Profile
        </Link>

        {/* Toggle de tema */}
        <ThemeToggle />

        {/* Botón Logout */}
        <button
          onClick={onLogout}
          className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700"
        >
          Logout
        </button>
      </div>
    </nav>
  );
}

export default Navbar;
