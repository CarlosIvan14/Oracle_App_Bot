import React from 'react';
import TaskListItem from './TaskListItem';

function TaskList({ items, toggleDone, deleteItem }) {
  return (
<<<<<<< Updated upstream
    <div id="maincontent" className="bg-customDark bg-opacity-50 p-4 rounded-lg">
=======
    <div id="maincontent" className="bg-gray-800 bg-opacity-50 p-4 rounded-lg">
>>>>>>> Stashed changes

      {/* Tareas pendientes */}
      <h2 className="text-2xl font-semibold mb-2 text-white">
        Pending Items
      </h2>
      <table
        id="itemlistNotDone"
        className="
          min-w-full 
          table-auto 
          w-full 
          break-words 
          divide-y 
          divide-gray-700 
          mb-4
        "
      >
<<<<<<< Updated upstream
        <tbody className="divide-y divide-customDarkligth">
=======
        <tbody className="divide-y divide-gray-700">
>>>>>>> Stashed changes
          {items
            .filter(item => !item.done)
            .map(item => (
              <TaskListItem
                key={item.id}
                item={item}
                toggleDone={toggleDone}
                deleteItem={deleteItem}
              />
            ))}
        </tbody>
      </table>

      {/* Tareas completadas */}
      <h2 id="donelist" className="text-2xl font-semibold mb-2 text-white">
        Done Items
      </h2>
      <table
        id="itemlistDone"
        className="
          min-w-full 
          table-auto 
          w-full 
          break-words 
          divide-y 
<<<<<<< Updated upstream
          divide-customDarkligth
        "
      >
        <tbody className="divide-y divide-customDarkligth">
=======
          divide-gray-700
        "
      >
        <tbody className="divide-y divide-gray-700">
>>>>>>> Stashed changes
          {items
            .filter(item => item.done)
            .map(item => (
              <TaskListItem
                key={item.id}
                item={item}
                toggleDone={toggleDone}
                deleteItem={deleteItem}
              />
            ))}
        </tbody>
      </table>
    </div>
  );
}

export default TaskList;
