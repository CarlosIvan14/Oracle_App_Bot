// src/routes/Home.js
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function Home({ user }) {
  const [projects, setProjects] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // Si no viene el usuario en props, intenta obtenerlo desde localStorage.
  const currentUser = user || JSON.parse(localStorage.getItem('user') || 'null');

  useEffect(() => {
    if (!currentUser || !currentUser.idUser) {
      setError("No se encontró usuario logueado");
      setLoading(false);
      return;
    }

    // Usamos el id del usuario activo para obtener sus proyectos
    fetch(`http://159.54.138.76/api/project-users/user/${currentUser.idUser}/projects`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Error al obtener proyectos');
        }
        return response.json();
      })
      .then((data) => {
        setProjects(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, [currentUser]);

  const filtered = projects.filter((p) =>
    p.name.toLowerCase().includes(search.toLowerCase())
  );

  const handleProjectClick = (id) => {
    navigate(`/projects/${id}`);
  };

  if (loading) {
    return <p className="text-center text-white mt-8">Cargando proyectos...</p>;
  }

  if (error) {
    return <p className="text-center mt-8 text-red-500">{error}</p>;
  }

  return (      
    <div className="p-6 ">
      <div className="mb-4 flex items-center justify-center">
        <h1 className="text-3xl text-white font-bold mb-4">Tus Proyectos</h1>
      </div>
      <div className="mb-4 flex items-center justify-center">
        <input
          className="border px-3 py-2 rounded-full w-full max-w-md bg-black bg-opacity-20 hover:bg-opacity-30 text-white placeholder-white"
          type="text"
          placeholder="Buscar proyectos..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Lista de proyectos */}
      <div className="space-y-2">
        {filtered.length > 0 ? (
          filtered.map((proj) => (
            <div
              key={proj.id_project}
              className="bg-black bg-opacity-20 p-4 rounded-2xl cursor-pointer hover:bg-opacity-30"
              onClick={() => handleProjectClick(proj.id_project)}
            >
              <h2 className="text-xl text-white font-semibold">{proj.name}</h2>
              {proj.description && (
                <p className="mt-2 text-sm text-gray-300">
                  {proj.description}
                </p>
              )}
              {proj.creationTs && (
                <p className="mt-1 text-xs text-gray-500">
                  Creado el: {new Date(proj.creationTs).toLocaleString()}
                </p>
              )}
            </div>
          ))
        ) : (
          <p className="text-gray-400">No se encontraron proyectos</p>
        )}
      </div>
    </div>
  );
}

export default Home;
