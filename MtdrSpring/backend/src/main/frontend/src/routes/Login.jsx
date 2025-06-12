// src/routes/Login.js
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import LoadingSpinner from "../components/LoadingSpinner";
import config from "../config";
import oracleLogo from "../Oracle-logo.png";


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
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, password }),
      });
      if (!response.ok) throw new Error("Usuario/contraseña inválidos");
      const data = await response.json();
      localStorage.setItem("user", JSON.stringify(data));
      onLogin(data);
      navigate("/home");
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    // fondo de pantalla y overlay
    <div
      className="min-h-screen flex items-center justify-center bg-cover bg-center"
      style={{ backgroundImage: "url('/login-bg.png')" }}
    >
      <div className="bg-black bg-opacity-60 p-8 rounded-2xl shadow-lg w-80">
        {/* logo Oracle */}
        <img
          src={oracleLogo}
          alt="Oracle Logo"
          className="h-12 mx-auto mb-4"
        />

        <h2 className="text-2xl text-white font-bold mb-4 text-center">
          Login
        </h2>

        {error && <p className="text-red-400 text-center">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="block text-white mb-1">Name</label>
            <input
              className="bg-gray-200 bg-opacity-30 w-full rounded-full px-4 py-2 text-white placeholder-gray-300 focus:outline-none"
              type="text"
              placeholder="Tu usuario"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="block text-white mb-1">Password</label>
            <input
              className="bg-gray-200 bg-opacity-30 w-full rounded-full px-4 py-2 text-white placeholder-gray-300 focus:outline-none"
              type="password"
              placeholder="********"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <button
            type="submit"
            className="bg-red-600 hover:bg-red-700 w-full text-white py-2 rounded-full mt-4 flex justify-center items-center"
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
