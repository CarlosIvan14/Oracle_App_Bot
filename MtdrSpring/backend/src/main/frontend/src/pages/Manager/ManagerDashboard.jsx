// App.js
import React, { useState, useEffect } from 'react';
import NewItem from '../../components/NewItem/NewItem';
import TaskList from '../../components/TaskList/TaskList';
import API_LIST from '../../API';

function ManagerDashboard() {
  const [isLoading, setLoading] = useState(false);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [error, setError] = useState();

  function deleteItem(deleteId) {
    fetch(API_LIST + "/" + deleteId, {
      method: 'DELETE',
    })
    .then(response => {
      if (response.ok) {
        const remainingItems = items.filter(item => item.id !== deleteId);
        setItems(remainingItems);
      } else {
        throw new Error('Something went wrong ...');
      }
    })
    .catch(error => {
      setError(error);
    });
  }

  function toggleDone(event, id, description, done) {
    event.preventDefault();
    modifyItem(id, description, done).then(
      (result) => { reloadOneIteam(id); },
      (error) => { setError(error); }
    );
  }

  function reloadOneIteam(id) {
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
          const items2 = items.map(x => (x.id === id ? result : x));
          setItems(items2);
        },
        (error) => {
          setError(error);
        }
      );
  }

  function modifyItem(id, description, done) {
    var data = {"description": description, "done": done};
    return fetch(API_LIST + "/" + id, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
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
        });
  }, []);

  function addItem(task) {
    setInserting(true);
    fetch(API_LIST, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
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
    <div className="ManagerDashboard">
      <h1 className="text-4xl text-white font-bold">MY TASK LIST</h1>
      <NewItem addItem={addItem} isInserting={isInserting} />
      {error && <p>Error: {error.message}</p>}
      {isLoading && (
        <div className="flex justify-center items-center my-4">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
        </div>
      )}
      {!isLoading && <TaskList items={items} toggleDone={toggleDone} deleteItem={deleteItem} />}
    </div>
  );
}

export default ManagerDashboard;