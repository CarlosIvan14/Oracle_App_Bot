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
    const stored = localStorage.getItem('user');
    if (stored) setUser(JSON.parse(stored));
  }, []);

  const handleLogin = u => {
    setUser(u);
    localStorage.setItem('user', JSON.stringify(u));
  };
  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('user');
    // opcional: localStorage.removeItem('projectUser')
  };

  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login onLogin={handleLogin} />} />

          <Route path="/" element={<PrivateLayout user={user} onLogout={handleLogout} />}>
            <Route path="home" element={<Home />} />

            {/* 1) Ficha normal de sprints */}
            <Route path="projects/:projectId" element={<ProjectSprints />} />

            <Route path="projects/:projectId/sprint/:sprintId" element={<SprintTasks />} />

            {/* Lista de usuarios */}
            <Route path="projects/:projectId/users" element={<UsersList />} />

            <Route path="reports" element={<Reports />} />
            <Route path="profile" element={<Profile user={user} />} />
          </Route>

          <Route path="*" element={<div className="p-6">Página no encontrada</div>} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
