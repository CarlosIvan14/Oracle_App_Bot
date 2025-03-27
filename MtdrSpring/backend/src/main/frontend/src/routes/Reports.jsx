// src/routes/Reports.js
import React from 'react';

function Reports() {
  // HARCODE: mostrar un “resumen” de tareas/hours
  const exampleReport = [
    { user: 'Developer User', tasksDone: 12, estimatedHours: 50 },
    { user: 'John', tasksDone: 5, estimatedHours: 20 },
  ];

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4">Reportes</h1>
      <p className="mb-4">Estadísticas de tus usuarios</p>
      <table className="min-w-full bg-black bg-opacity-20">
        <thead>
          <tr>
            <th className="px-4 py-2 text-left">Usuario</th>
            <th className="px-4 py-2 text-left">Tareas Completadas</th>
            <th className="px-4 py-2 text-left">Horas Estimadas</th>
          </tr>
        </thead>
        <tbody>
          {exampleReport.map((r, idx) => (
            <tr key={idx} className="border-b border-gray-600">
              <td className="px-4 py-2">{r.user}</td>
              <td className="px-4 py-2">{r.tasksDone}</td>
              <td className="px-4 py-2">{r.estimatedHours}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Reports;
