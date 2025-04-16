// src/components/Navbar.js
import React, { useState, useEffect } from 'react';
import { NavLink, useMatch, useNavigate, useParams } from 'react-router-dom';

const IconProfile = () => (
  <div className="w-8 h-8 bg-white rounded-full flex items-center justify-center text-red-600">
    👤
  </div>
);
const IconUsers = () => <span className="text-lg">👥</span>;
const IconPlus  = () => <span className="text-lg">＋</span>;

export default function Navbar({ onLogout }) {
  const navigate      = useNavigate();
  const matchHome     = useMatch('/home');
  const matchProject  = useMatch('/projects/:projectId/*');
  const matchSprints  = useMatch({ path: '/projects/:projectId', end: true });
  const matchUsers    = useMatch('/projects/:projectId/users');
  const { projectId } = useParams();

  // Obtener rol de projectUser (para mostrar Ver Usuarios y Añadir Sprint)
  const [roleUser, setRoleUser] = useState(null);
  useEffect(() => {
    if (matchProject && projectId) {
      const u = JSON.parse(localStorage.getItem('user'));
      fetch(
        `http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${u.idUser}`
      )
        .then(r => r.ok ? r.text() : Promise.reject())
        .then(txt => setRoleUser(txt.trim()))
        .catch(() => setRoleUser(null));
    }
  }, [matchProject, projectId]);

  const handleAddSprint = () => {
    window.dispatchEvent(new CustomEvent('openAddSprint'));
  };

  return (
    <nav className="w-full py-4 px-6 flex items-center justify-between bg-transparent text-white">
      {/* Logo – sin subrayado */}
      <div className="font-bold text-xl">
        <NavLink to="/home" className="text-white">Oracle Task Manager</NavLink>
      </div>

      <div className="flex items-center space-x-6">
        {/* Proyectos */}
        <NavLink to="/home" className="relative font-bold text-white">
          Proyectos
          <span
            className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
              matchHome ? 'w-full' : 'w-0'
            }`}
          />
        </NavLink>

        {/* Sólo dentro de un proyecto */}
        {matchProject && projectId && (
          <>
            {/* Sprints */}
            <NavLink
              to={`/projects/${projectId}`}
              className="relative font-bold text-white"
            >
              Sprints del Proyecto {projectId}
              <span
                className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
                  matchSprints && !matchUsers ? 'w-full' : 'w-0'
                }`}
              />
            </NavLink>

            {/* Sólo managers */}
            {roleUser === 'manager' && (
              <>
                 {!matchUsers && (
                  <button
                    onClick={handleAddSprint}
                    className="flex items-center font-bold text-white hover:text-gray-200"
                  >
                    <IconPlus />
                    <span className="ml-1">Añadir Sprint</span>
                  </button>
                )}

                <NavLink
                  to={`/projects/${projectId}/users`}
                  className="relative flex items-center font-bold text-white"
                >
                  <IconUsers />
                  <span className="ml-1">Ver Usuarios</span>
                  <span
                    className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
                      matchUsers ? 'w-full' : 'w-0'
                    }`}
                  />
                </NavLink>
              </>
            )}
          </>
        )}

        {/* Perfil / Logout */}
        <div className="relative group">
          <button className="focus:outline-none">
            <IconProfile />
          </button>
          <div className="absolute right-0 mt-2 w-32 bg-white text-black rounded-lg shadow-lg opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={() => navigate('/profile')}
              className="w-full text-left px-4 py-2 hover:bg-gray-200"
            >
              Perfil
            </button>
            <button
              onClick={onLogout}
              className="w-full text-left px-4 py-2 hover:bg-gray-200"
            >
              Cerrar sesión
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
