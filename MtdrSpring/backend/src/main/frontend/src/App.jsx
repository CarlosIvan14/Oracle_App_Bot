// src/App.js
import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';

import Login from './routes/Login';
import Home from './routes/Home';
import ProjectSprints from './routes/ProjectSprints';
import SprintTasks from './routes/SprintTasks';
import UsersList from './routes/UsersList';
import Reports from './routes/Reports';
import Profile from './routes/Profile';
import PrivateLayout from './components/PrivateLayout';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Si hay un user guardado en localStorage, cargarlo
    const stored = localStorage.getItem('user');
    if (stored) {
      setUser(JSON.parse(stored));
    }
  }, []);

  const handleLogin = (loggedUser) => {
    setUser(loggedUser);
    localStorage.setItem('user', JSON.stringify(loggedUser));
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          {/* Ruta para login */}
          <Route path="/" element={<Login onLogin={handleLogin} />} />

          {/* Rutas privadas (exigen user) */}
          <Route
            path="/"
            element={<PrivateLayout user={user} onLogout={handleLogout} />}
          >
            <Route path="home" element={<Home />} />
            <Route path="projects/:projectId" element={<ProjectSprints />} />
            <Route path="projects/:projectId/sprint/:sprintId" element={<SprintTasks />} />
            <Route path="/projects/:projectId/users" element={<UsersList />} />
            <Route path="reports" element={<Reports />} />
            <Route path="profile" element={<Profile user={user} />} />
          </Route>

          {/* 404 fallback */}
          <Route path="*" element={<div className="p-6">Página no encontrada</div>} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
