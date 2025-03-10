import React from 'react';
import TaskListItem from './TaskListItem';

function TaskList({ items, toggleDone, deleteItem }) {
  return (
    <div id="maincontent">
      {/* Table for Not Done items */}
      <table id="itemlistNotDone" className="min-w-full divide-y divide-gray-200 mb-4">
        <tbody className="bg-white divide-y divide-gray-200">
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

      {/* Heading for Done items */}
      <h2 id="donelist" className="text-2xl font-semibold mb-2">Done items</h2>

      {/* Table for Done items */}
      <table id="itemlistDone" className="min-w-full divide-y divide-gray-200">
        <tbody className="bg-white divide-y divide-gray-200">
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