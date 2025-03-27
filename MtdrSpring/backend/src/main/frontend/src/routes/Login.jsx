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
      // Harcodeado o real
      // Ejemplo HARCODE para test:
      if (name === 'manager' && password === '123') {
        onLogin({ idUser: 1, name: 'Manager User', role: 'manager' });
        navigate('/home');
      } else if (name === 'dev' && password === '123') {
        onLogin({ idUser: 2, name: 'Developer User', role: 'developer' });
        navigate('/home');
      } else {
        throw new Error('Usuario/contraseña inválidos');
      }
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
              className="bg-gray-200 w-full rounded-full px-2 py-1"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="block text-white">Password</label>
            <input
              className="bg-gray-200 w-full rounded-full px-2 py-1"
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
