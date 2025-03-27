// components/Login/Login.js
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
      const response = await fetch('http://localhost:8081/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name, password }),
      });

      if (!response.ok) {
        throw new Error('Invalid username or password');
      }

      const userData = await response.json();

     // Access properties directly from `userData`
     const idUser = userData.idUser;
     const userName = userData.name;
     const role = userData.role;
 
     // Call onLogin with the necessary data
     onLogin({ idUser, name: userName, role });

      // Navigate based on role
      if (role === 'manager') {
        navigate('/manager');
      } else if (role === 'developer') {
        navigate('/developer');
      } else {
        setError('Unknown role');
      }
    } catch (error) {
      setError(error.message);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-b from-customDarkligth to-customDark">
      <div className="bg-customDark bg-opacity-90 p-10 rounded-2xl shadow-2xl w-96 h-97">
        <h2 className="text-3xl font-bold text-center text-white mb-6">Login</h2>
        
        {error && <p className="text-red-700 text-center mb-4">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-white font-medium">Name</label>
            <input
              className="w-full border text-white bg-customDarkligth border-gray-800 px-4 py-2 rounded-3xl focus:outline-none focus:ring-2 focus:ring-gray-300 transform transition duration-200 hover:scale-105"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="pb-6">
            <label className="block text-white font-medium">Password</label>
            <input
              className="w-full border text-white bg-customDarkligth border-gray-800 px-4 py-2 rounded-3xl focus:outline-none focus:ring-2 focus:ring-gray-300 transform transition duration-200 hover:scale-105"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button 
            type="submit" 
            className="w-full bg-red-500 text-white py-2 rounded-full font-semibold transform transition duration-200 hover:scale-105 focus:outline-none focus:ring-2 focus:ring-gray-300"
          >
            Log In
          </button>

        </form>
      </div>
    </div>
  );  
}

export default Login;