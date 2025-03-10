import React from 'react';
import ReactDOM from 'react-dom/client'; // Import from 'react-dom/client' in React 18
import './index.css';
import App from './App';

// Create a root and render the app using createRoot()
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
