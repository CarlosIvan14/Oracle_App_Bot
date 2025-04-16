// src/routes/Login.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login({ onLogin }) {
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch("http://localhost:8081/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ name, password })
      });

      if (!response.ok) {
        throw new Error('Usuario/contraseña inválidos');
      }

      const data = await response.json();
      // Guardamos el usuario en el almacenamiento local
      localStorage.setItem('user', JSON.stringify(data));
      // Actualiza el estado global o del componente padre si es necesario
      onLogin(data);
      navigate('/home');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-[#7f1d1d] to-[#161513]">
      <div className="bg-black bg-opacity-50 p-8 rounded-2xl shadow-lg w-80">
        <h2 className="text-2xl text-white font-bold mb-4">Login</h2>
        {error && <p className="text-red-400">{error}</p>}
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-white">Name</label>
            <input
              className="bg-gray-200 bg-opacity-50 w-full rounded-full px-2 py-1"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="block text-white">Password</label>
            <input
              className="bg-gray-200 bg-opacity-50 w-full rounded-full px-2 py-1"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button
            type="submit"
            className="bg-red-600 w-full text-white py-2 rounded-full mt-4 hover:bg-red-700"
          >
            Ingresar
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;
