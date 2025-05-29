// src/routes/Login.js
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import LoadingSpinner from "../components/LoadingSpinner";
import config from "../config";

function Login({ onLogin }) {
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await fetch(`${config.apiBaseUrl}/users/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name, password }),
      });

      if (!response.ok) {
        throw new Error("Usuario/contraseña inválidos");
      }

      const data = await response.json();
      // Guardamos el usuario en el almacenamiento local
      localStorage.setItem("user", JSON.stringify(data));
      // Actualiza el estado global o del componente padre si es necesario
      onLogin(data);
      navigate("/home");
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#212233]">
      <div className="bg-black bg-opacity-50 p-8 rounded-2xl shadow-lg w-80">
        <h2 className="text-2xl text-white font-bold mb-4">Login test 4</h2>
        {error && <p className="text-red-400">{error}</p>}
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-white">Name</label>
            <input
              className="bg-gray-200 bg-opacity-50 w-full rounded-full px-2 py-1 text-white"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="block text-white">Password</label>
            <input
              className="bg-gray-200 bg-opacity-50 w-full rounded-full px-2 py-1 text-white"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <button
            type="submit"
            className="bg-red-600 w-full text-white py-2 rounded-full mt-4 hover:bg-red-700 flex justify-center items-center"
            disabled={isLoading}
          >
            {isLoading ? <LoadingSpinner /> : "Ingresar"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;
