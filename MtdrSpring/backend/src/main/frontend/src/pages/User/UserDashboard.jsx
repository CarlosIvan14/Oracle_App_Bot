import React from 'react';

function UserDashboard() {
  const user = JSON.parse(localStorage.getItem('user')); // Retrieve the user from localStorage

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-2xl shadow-lg w-96 text-center">
        <h2 className="text-3xl font-bold text-gray-800 mb-4">Welcome, {user?.username || 'User'}!</h2>
        <p className="text-gray-600">You have successfully logged in as a User.</p>
      </div>
    </div>
  );
}

export default UserDashboard;
