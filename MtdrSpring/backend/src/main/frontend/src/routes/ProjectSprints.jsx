// src/routes/ProjectSprints.js
import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';

// HARCODE sprints
const SPRINTS_DATA = {
  101: [
    { id: 1, name: 'Sprint 1', status: 'Activo' },
    { id: 2, name: 'Sprint 2', status: 'Activo' },
    { id: 3, name: 'Sprint 3', status: 'Cerrado' },
    { id: 4, name: 'Sprint 4', status: 'Activo' },
  ],
  102: [
    { id: 5, name: 'Sprint 1', status: 'Activo' },
    { id: 6, name: 'Sprint 2', status: 'Cerrado' },
  ],
  103: [
    { id: 7, name: 'Sprint A', status: 'Activo' },
    { id: 8, name: 'Sprint B', status: 'Activo' },
  ],
  104: [
    { id: 9, name: 'Sprint Uno', status: 'Activo' },
    { id: 10, name: 'Sprint Dos', status: 'Activo' },
    { id: 11, name: 'Sprint Tres', status: 'Cerrado' },
  ],
};

function ProjectSprints() {
  const { projectId } = useParams();
  const navigate = useNavigate();

  const sprints = SPRINTS_DATA[projectId] || [];

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4">Sprints del Proyecto {projectId}</h1>
      <div className="grid grid-cols-4 gap-4">
        {sprints.map(s => (
          <div
            key={s.id}
            className="bg-black bg-opacity-20 p-4 rounded-2xl hover:bg-opacity-30 cursor-pointer"
            onClick={() => navigate(`/projects/${projectId}/sprint/${s.id}`)}
          >
            <h2 className="text-xl font-semibold">{s.name}</h2>
            <p>Status: {s.status}</p>
          </div>
        ))}
        {sprints.length === 0 && (
          <p className="col-span-4 text-gray-400">No hay sprints.</p>
        )}
      </div>
    </div>
  );
}

export default ProjectSprints;
