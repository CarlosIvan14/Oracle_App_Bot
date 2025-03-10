import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from '../pages/Login/Login';
import Dashboard from '../pages/Dashboard';


function AppRouter() {
  const [user, setUser] = useState(null);

  // Check localStorage for user data on initial load
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData)); // Store user data in localStorage
  };


  return (
    <Router>
      <Routes>
        {/* Home Page (Temporary Redirect to Login if No User) */}
        <Route path="/" element={!user ? <Login onLogin={handleLogin} /> : <Navigate to={user.role === 'manager' ? '/manager' : '/user'} />} />

        {/* Manager Dashboard (Protected Route) */}
        <Route path="/manager" element={user?.role === 'manager' ? <Dashboard /> : <Navigate to="/" />} />

        {/* User Dashboard (Protected Route) */}
        <Route path="/user" element={user?.role === 'user' ? <Dashboard /> : <Navigate to="/" />} />

        {/* Fallback Route */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default AppRouter;
