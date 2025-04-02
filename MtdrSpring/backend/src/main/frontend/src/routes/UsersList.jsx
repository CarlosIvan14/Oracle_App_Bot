// src/routes/UsersList.js
import React from 'react';

function UsersList() {
  // Harcodea la lista o usa tu fetch real
  const users = [
    { id: 1, name: 'Manager User', role: 'manager', skill: 'Leadership' },
    { id: 2, name: 'Developer User', role: 'developer', skill: 'React' },
    { id: 3, name: 'John', role: 'developer', skill: 'Java' },
  ];

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-4">Lista de Usuarios</h1>
      <table className="min-w-full bg-black bg-opacity-20">
        <thead>
          <tr>
            <th className="px-4 py-2 text-left">ID</th>
            <th className="px-4 py-2 text-left">Nombre</th>
            <th className="px-4 py-2 text-left">Rol</th>
            <th className="px-4 py-2 text-left">Skill</th>
          </tr>
        </thead>
        <tbody>
          {users.map(u => (
            <tr key={u.id} className="border-b border-gray-600">
              <td className="px-4 py-2">{u.id}</td>
              <td className="px-4 py-2">{u.name}</td>
              <td className="px-4 py-2">{u.role}</td>
              <td className="px-4 py-2">{u.skill}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default UsersList;
