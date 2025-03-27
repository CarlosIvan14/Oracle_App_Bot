// src/routes/Home.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const PROJECTS_DATA = [
  { id: 101, name: 'Proyecto Oracle Migration' },
  { id: 102, name: 'Proyecto Billing System' },
  { id: 103, name: 'Proyecto Web Chatbot' },
  { id: 104, name: 'Proyecto Mobile Banking' },
];

function Home() {
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const filtered = PROJECTS_DATA.filter(p =>
    p.name.toLowerCase().includes(search.toLowerCase())
  );

  const handleProjectClick = (id) => {
    // Navegación interna sin recargar la página
    navigate(`/projects/${id}`);
  };

  return (
    <div className="p-6 bg-gray-100 dark:bg-customDark">
      <div className="mb-4 flex items-center justify-center">
        <h1 className="text-3xl font-bold mb-4">Tus Proyectos</h1>
      </div>
      <div className="mb-4 flex items-center justify-center">
        <input
          className="border px-3 py-2 rounded-full w-full max-w-md bg-black bg-opacity-20 hover:bg-opacity-30 text-black dark:text-white
    placeholder-black dark:placeholder-white "
          type="text"
          placeholder="Buscar proyectos..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Lista de proyectos */}
      <div className="space-y-2">
        {filtered.map(proj => (
          <div
            key={proj.id}
            className="bg-black bg-opacity-20 p-4 rounded-2xl cursor-pointer hover:bg-opacity-30"
            onClick={() => handleProjectClick(proj.id)}
          >
            <h2 className="text-xl font-semibold">{proj.name}</h2>
          </div>
        ))}
        {filtered.length === 0 && (
          <p className="text-gray-400">No se encontraron proyectos</p>
        )}
      </div>
    </div>
  );
}

export default Home;
