// src/components/Navbar.jsx
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
  const { projectId } = useParams();

  // Todos los hooks de useMatch al inicio
  const matchHome        = useMatch('/home');
  const matchSprintRoot  = useMatch({ path: '/projects/:projectId', end: true });
  const matchSprintTasks = useMatch('/projects/:projectId/sprint/:sprintId');
  const matchAllTasks    = useMatch('/projects/:projectId/sprint/:sprintId/all');
  const matchUsers       = useMatch('/projects/:projectId/users');
  const matchReports = useMatch('/projects/:projectId/reports');

  // Obtener rol solo en la ruta raíz de sprints
  const [roleUser, setRoleUser] = useState(null);
  useEffect(() => {
    if (matchSprintRoot && projectId) {
      const u = JSON.parse(localStorage.getItem('user'));
      fetch(
        `http://localhost:8081/api/project-users/role-user/project-id/${projectId}/user-id/${u.idUser}`
      )
        .then(r => r.ok ? r.text() : Promise.reject())
        .then(txt => setRoleUser(txt.trim()))
        .catch(() => setRoleUser(null));
    }
  }, [matchSprintRoot, projectId]);

  const handleAddSprint = () => {
    window.dispatchEvent(new CustomEvent('openAddSprint'));
  };

  return (
    <nav className="w-full py-4 px-6 flex items-center justify-between bg-transparent text-white">
      {/* Logo */}
      <div className="font-bold text-xl">
        <NavLink to="/home" className="text-white">Oracle Task Manager</NavLink>
      </div>

      <div className="flex items-center space-x-6">
        {/* Proyectos (siempre) */}
        <NavLink to="/home" className="relative font-bold text-white">
          Proyectos
          <span
            className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
              matchHome ? 'w-full' : 'w-0'
            }`}
          />
        </NavLink>

        {/* RUTA: /projects/:projectId exacto */}
        {matchSprintRoot && !matchSprintTasks && !matchAllTasks && !matchUsers && (
          <>
            <NavLink
              to={`/projects/${projectId}`}
              className="relative font-bold text-white"
            >
              Sprints del Proyecto {projectId}
              <span
                className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
                  matchSprintRoot ? 'w-full' : 'w-0'
                }`}
              />
            </NavLink>
            <NavLink
                  to={`/projects/${projectId}/reports`}
                  className="relative font-bold text-white"
                >
                  Reportes
                  <span
                    className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
                      matchReports ? 'w-full' : 'w-0'
                    }`}
                  />
            </NavLink>
            {roleUser === 'manager' && (
              <>
                <button
                  onClick={handleAddSprint}
                  className="flex items-center font-bold text-white hover:text-gray-200"
                >
                  <IconPlus /><span className="ml-1">Añadir Sprint</span>
                </button>
                <NavLink
                  to={`/projects/${projectId}/users`}
                  className="relative flex items-center font-bold text-white"
                >
                  <IconUsers /><span className="ml-1">Ver Usuarios</span>
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

        {/* RUTA: /projects/:projectId/users */}
        {matchUsers && (
          <>
            <NavLink
              to={`/projects/${projectId}`}
              className="relative font-bold text-white"
            >
              Sprints del Proyecto {projectId}
            </NavLink>
            <NavLink
              to={`/projects/${projectId}/users`}
              className="relative font-bold text-white"
            >
              Ver Usuarios
              <span
                className="absolute bottom-[-2px] left-0 h-[2px] bg-white w-full"
              />
            </NavLink>
          </>
        )}
      {/* RUTA: /projects/:projectId/reports */}
      {matchReports && (
        <>
          {/* enlace al tablero de sprints */}
          <NavLink
            to={`/projects/${projectId}`}
            className="relative font-bold text-white"
          >
            Sprints del Proyecto {projectId}
          </NavLink>

          {/* enlace al propio reporte (marcado como activo) */}
          <NavLink
            to={`/projects/${projectId}/reports`}
            className="relative font-bold text-white"
          >
            Reportes
            <span className="absolute bottom-[-2px] left-0 h-[2px] bg-white w-full" />
          </NavLink>
        </>
      )}

        {/* RUTA: /projects/:projectId/sprint/:sprintId */}
        {matchSprintTasks && !matchAllTasks && (
          <>
            <NavLink
              to={`/projects/${projectId}`}
              className="relative font-bold text-white"
            >
              Sprints del Proyecto {projectId}
            </NavLink>
            <NavLink
              to={`/projects/${projectId}/sprint/${matchSprintTasks.params.sprintId}/all`}
              className="relative font-bold text-white"
            >
              Todas las tareas
              <span
                className={`absolute bottom-[-2px] left-0 h-[2px] bg-white transition-all ${
                  matchAllTasks ? 'w-full' : 'w-0'
                }`}
              />
            </NavLink>
           
          </>
        )}

        {/* RUTA: /projects/:projectId/sprint/:sprintId/all */}
        {matchAllTasks && (
          <>
            <NavLink
              to={`/projects/${projectId}`}
              className="relative font-bold text-white"
            >
              Sprints del Proyecto {projectId}
            </NavLink>
          </>
        )}

        {/* Icono perfil + dropdown (siempre) */}
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
