import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaUsers } from 'react-icons/fa'; // Ejemplo de ícono de usuarios
import NewItemModal from '../components/NewItem/NewItemModal';
import TaskList from '../components/TaskList/TaskList';
import API_LIST from '../API';
import NewUserModal from '../components/NewUserModal/NewUserModal';

function Dashboard() {
  const [isLoading, setLoading] = useState(false);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [error, setError] = useState();
  const [showUserModal, setShowUserModal] = useState(false);
  const [isRegistering, setRegistering] = useState(false);

  const navigate = useNavigate();
  const user = useMemo(() => {
    const storedUser = localStorage.getItem('user');
    return storedUser ? JSON.parse(storedUser) : null;
  }, []);

  const isManager = user && user.role === 'manager';

  const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
  };

  useEffect(() => {
    if (user?.idUser) {
      setLoading(true);
      const url = user.role === 'manager' 
        ? API_LIST
        : `${API_LIST}/user/${user.idUser}`;

      fetch(url)
        .then(response => response.json())
        .then(result => {
          setItems(result);
          setLoading(false);
        })
        .catch(error => {
          setError(error);
          setLoading(false);
        });
    }
  }, []);

  function deleteItem(deleteId) {
    fetch(API_LIST + "/" + deleteId, { method: 'DELETE' })
      .then(response => {
        if (response.ok) {
          const remainingItems = items.filter(item => item.id !== deleteId);
          setItems(remainingItems);
        } else {
          throw new Error('Something went wrong ...');
        }
      })
      .catch(error => setError(error));
  }

  function toggleDone(event, id, description, done) {
    event.preventDefault();
    modifyItem(id, description, done).then(
      () => reloadOneIteam(id),
      error => setError(error)
    );
  }

  function reloadOneIteam(id) {
    fetch(API_LIST + "/" + id)
      .then(response => {
        if (response.ok) return response.json();
        else throw new Error('Something went wrong ...');
      })
      .then(
        result => {
          const items2 = items.map(x => (x.id === id ? result : x));
          setItems(items2);
        },
        error => setError(error)
      );
  }

  function modifyItem(id, description, done) {
    const data = { description, done };
    return fetch(API_LIST + "/" + id, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
      .then(response => {
        if (response.ok) return response;
        else throw new Error('Something went wrong ...');
      });
  }

  function addItem(task) {
    setInserting(true);
    fetch(API_LIST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task),
    })
      .then(response => {
        if (response.ok) return response;
        else throw new Error('Something went wrong ...');
      })
      .then(result => {
        const id = result.headers.get('location');
        const newItem = { ...task, id };
        setItems([newItem, ...items]);
        setInserting(false);
      })
      .catch(error => {
        setInserting(false);
        setError(error);
      });
  }

  const handleCloseUserModal = () => {
    setShowUserModal(false);
  };

  const handleOpenUserModal = () => {
    setShowUserModal(true);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-700 to-gray-950 flex flex-col items-center">
      {isManager && (
        <div className="mt-10 flex gap-4 justify-center">
          <NewItemModal addItem={addItem} isInserting={isInserting} />
          <button
          onClick={handleOpenUserModal}
          className="
            flex items-center 
            bg-transparent 
            text-white 
            font-semibold 
            py-2 
            px-4 
            rounded-full
            transition 
            duration-200
            transform hover:scale-105
            hover:border hover:border-purple-500
          "
        >
          Registrar Usuario
        </button>
          <NewUserModal
          isOpen={showUserModal}
          onClose={handleCloseUserModal}
          isRegistering={isRegistering}/>

          {/* Botón para ir a la lista de usuarios */}
          <Link
            to="/users"
            className="
              flex items-center
              bg-transparent
              text-white
              font-semibold
              py-2
              px-4
              rounded-full
              transition
              duration-200
              transform hover:scale-105
              hover:border hover:border-blue-500
            "
          >
            <FaUsers className="mr-2" />
            Ver Usuarios
          </Link>

          <button
            onClick={handleLogout}
            className="
              bg-transparent
              text-white
              font-semibold
              py-2
              px-4
              rounded-full
              transition
              duration-200
              transform hover:scale-105
              hover:border hover:border-red-500
            "
          >
            Cerrar Sesión
          </button>
        </div>
      )}

      {!isManager && (
        <div className="mt-10 flex justify-center">
          <button
            onClick={handleLogout}
            className="
              bg-transparent
              text-white
              font-semibold
              py-2
              px-4
              rounded-full
              transition
              duration-200
              transform hover:scale-105
              hover:border hover:border-red-500
            "
          >
            Cerrar Sesión
          </button>
        </div>
      )}

      {/* Contenedor para la lista de tareas */}
      <div className="w-full max-w-4xl bg-black bg-opacity-40 backdrop-blur-md p-8 rounded-xl shadow-xl mt-8">
        <h1 className="text-4xl text-white font-bold mb-6 text-center">
          MY TASK LIST
        </h1>

        {error && (
          <p className="text-red-400 text-center mt-4">
            Error: {error.message}
          </p>
        )}

        {isLoading ? (
          <div className="flex justify-center items-center my-4">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-300"></div>
          </div>
        ) : (
          <TaskList
            items={items}
            toggleDone={toggleDone}
            deleteItem={deleteItem}
          />
        )}
      </div>
    </div>
  );
}

export default Dashboard;
