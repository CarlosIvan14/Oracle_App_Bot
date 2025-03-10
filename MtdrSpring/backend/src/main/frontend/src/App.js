/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * Esta es la aplicación principal en React utilizando componentes funcionales.
 */
import React, { useState, useEffect } from 'react';
import NewItem from './NewItem';
import API_LIST from './API';

// Helper para formatear fechas, similar a react-moment
function formatDate(dateString) {
  const options = { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' };
  return new Date(dateString).toLocaleString('en-US', options);
}

function App() {
  const [isLoading, setLoading] = useState(false);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [error, setError] = useState();

  function deleteItem(deleteId) {
    fetch(API_LIST + "/" + deleteId, { method: 'DELETE' })
      .then(response => {
        if (response.ok) {
          return response;
        } else {
          throw new Error('Something went wrong ...');
        }
      })
      .then(
        () => {
          const remainingItems = items.filter(item => item.id !== deleteId);
          setItems(remainingItems);
        },
        (error) => {
          setError(error);
        }
      );
  }

  function toggleDone(event, id, description, done) {
    event.preventDefault();
    modifyItem(id, description, done).then(
      () => { reloadOneItem(id); },
      (error) => { setError(error); }
    );
  }

  function reloadOneItem(id) {
    fetch(API_LIST + "/" + id)
      .then(response => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error('Something went wrong ...');
        }
      })
      .then(
        (result) => {
          const updatedItems = items.map(x => (x.id === id ? result : x));
          setItems(updatedItems);
        },
        (error) => {
          setError(error);
        }
      );
  }

  function modifyItem(id, description, done) {
    const data = { "description": description, "done": done };
    return fetch(API_LIST + "/" + id, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
      .then(response => {
        if (response.ok) {
          return response;
        } else {
          throw new Error('Something went wrong ...');
        }
      });
  }

  useEffect(() => {
    setLoading(true);
    fetch(API_LIST)
      .then(response => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error('Something went wrong ...');
        }
      })
      .then(
        (result) => {
          setLoading(false);
          setItems(result);
        },
        (error) => {
          setLoading(false);
          setError(error);
        }
      );
  }, []);

  function getPriorityLabel(priority) {
    switch (priority) {
      case 1:
        return "Alta";
      case 2:
        return "Media";
      case 3:
        return "Baja";
      default:
        return "Sin definir";
    }
  }

  function addItem(task) {
    console.log("addItem", task);
    setInserting(true);
    fetch(API_LIST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task),
    })
      .then(response => {
        if (response.ok) {
          return response;
        } else {
          throw new Error('Something went wrong ...');
        }
      })
      .then((result) => {
        const id = result.headers.get('location');
        const newItem = { ...task, id: id };
        setItems([newItem, ...items]);
        setInserting(false);
      })
      .catch((error) => {
        setInserting(false);
        setError(error);
      });
  }

  return (
    <div className="App p-4">
      <h1 className="text-3xl font-bold mb-4">MY TASK LIST</h1>
      <NewItem addItem={addItem} isInserting={isInserting} />
      {error && <p className="text-red-500">Error: {error.message}</p>}
      {isLoading && (
        <div className="flex justify-center items-center my-4">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
        </div>
      )}
      {!isLoading && (
        <div id="maincontent">
          <table id="itemlistNotDone" className="min-w-full divide-y divide-gray-200 mb-4">
            <tbody className="bg-white divide-y divide-gray-200">
              {items.map(item => (
                !item.done && (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.description}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formatDate(item.createdAt)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.deadline}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{getPriorityLabel(item.priority)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.assignedUser ? item.assignedUser.name : "Sin usuario"}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-2 rounded text-xs"
                        onClick={(event) => toggleDone(event, item.id, item.description, !item.done)}>
                        Done
                      </button>
                    </td>
                  </tr>
                )
              ))}
            </tbody>
          </table>
          <h2 id="donelist" className="text-2xl font-semibold mb-2">Done items</h2>
          <table id="itemlistDone" className="min-w-full divide-y divide-gray-200">
            <tbody className="bg-white divide-y divide-gray-200">
              {items.map(item => (
                item.done && (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.description}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formatDate(item.createdAt)}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        className="bg-green-500 hover:bg-green-700 text-white font-bold py-1 px-2 rounded text-xs"
                        onClick={(event) => toggleDone(event, item.id, item.description, !item.done)}>
                        Undo
                      </button>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        className="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded text-xs flex items-center"
                        onClick={() => deleteItem(item.id)}>
                        {/* Ícono "x" con SVG, sustituye a DeleteIcon */}
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                        Delete
                      </button>
                    </td>
                  </tr>
                )
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default App;
