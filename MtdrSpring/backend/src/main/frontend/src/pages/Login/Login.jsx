// components/Login/Login.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();

    // Simulate authentication (replace with actual API call)
    if (username === 'manager' && password === '123') {
      onLogin({ username, role: 'manager' }); // Pass user role
      navigate('/manager'); // Redirect to admin dashboard
    } else if (username === 'user' && password === '123') {
      onLogin({ username, role: 'user' }); // Pass user role
      navigate('/user'); // Redirect to user dashboard
    } else {
      setError('Invalid username or password');
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-2xl shadow-lg w-96">
        <h2 className="text-3xl font-bold text-center text-gray-800 mb-6">Login</h2>
        
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
  
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-gray-700 font-medium">Username</label>
            <input
              className="w-full border border-gray-300 px-4 py-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
  
          <div>
            <label className="block text-gray-700 font-medium">Password</label>
            <input
              className="w-full border border-gray-300 px-4 py-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
  
          <button 
            type="submit" 
            className="w-full bg-blue-600 text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition duration-200"
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );  
}

export default Login;