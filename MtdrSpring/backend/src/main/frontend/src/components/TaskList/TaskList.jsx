import React from 'react';
import TaskListItem from './TaskListItem';

function TaskList({ items, toggleDone, deleteItem }) {
  return (
    <div id="maincontent" className="bg-gray-800 bg-opacity-50 p-4 rounded-lg">
      <table id="itemlistNotDone" className="min-w-full divide-y divide-gray-700 mb-4">
        <tbody className="divide-y divide-gray-700">
          {items.map(item => (
            !item.done && (
              <TaskListItem 
                key={item.id} 
                item={item} 
                toggleDone={toggleDone} 
                deleteItem={deleteItem} 
              />
            )
          ))}
        </tbody>
      </table>

      <h2 id="donelist" className="text-2xl font-semibold mb-2 text-white">
        Done items
      </h2>

      <table id="itemlistDone" className="min-w-full divide-y divide-gray-700">
        <tbody className="divide-y divide-gray-700">
          {items.map(item => (
            item.done && (
              <TaskListItem 
                key={item.id} 
                item={item} 
                toggleDone={toggleDone} 
                deleteItem={deleteItem} 
              />
            )
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TaskList;
