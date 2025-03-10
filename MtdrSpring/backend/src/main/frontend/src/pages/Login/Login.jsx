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
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-gray-700 to-gray-950">
      <div className="bg-slate-800 bg-opacity-90 p-10 rounded-2xl shadow-2xl w-96 h-96" >
        <h2 className="text-3xl font-bold text-center text-white mb-6">Login</h2>
        
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-white font-medium">Username</label>
            <input
              className="
                w-full 
                border 
                text-white
                bg-slate-600 
                border-gray-800 
                px-4 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-blue-500 
                transform 
                transition 
                duration-200 
                hover:scale-105
              "
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className='pb-6'>
            <label className="block text-white font-medium">Password</label>
            <input
              className="
                w-full 
                border 
                text-white
                bg-slate-600 
                border-gray-800 
                px-4 
                py-2 
                rounded-3xl 
                focus:outline-none 
                focus:ring-2 
                focus:ring-blue-500 
                transform 
                transition 
                duration-200 
                hover:scale-105
              "
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button 
            type="submit" 
            className="
              w-full 
              bg-gradient-to-bl from-cyan-900 to-purple-700 
              text-white 
              py-2 
              rounded-full 
              font-semibold 
              transform 
              transition 
              duration-200 
              hover:scale-105
            "
          >
            Log In
          </button>

        </form>
      </div>
    </div>

  );  
}

export default Login;