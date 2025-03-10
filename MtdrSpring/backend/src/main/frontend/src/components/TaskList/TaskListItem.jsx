import React from 'react';

function formatDate(dateString) {
  const options = { 
    month: 'short', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  };
  return new Date(dateString).toLocaleString('en-US', options);
}

function getPriorityLabel(priority) {
  switch (priority) {
    case 1: return "Alta";
    case 2: return "Media";
    case 3: return "Baja";
    default: return "Sin definir";
  }
}

function TaskListItem({ item, toggleDone, deleteItem }) {
  return (
    <tr className="hover:bg-gray-700 text-white">
      <td className="px-6 py-3 whitespace-nowrap">{item.description}</td>
      <td className="px-6 py-3 whitespace-nowrap">
        {item.createdAt ? formatDate(item.createdAt) : "--"}
      </td>
      <td className="px-6 py-3 whitespace-nowrap">
        {item.deadline || "--"}
      </td>
      <td className="px-6 py-3 whitespace-nowrap">
        {getPriorityLabel(item.priority)}
      </td>
      <td className="px-6 py-3 whitespace-nowrap">
        {item.assignedUser ? item.assignedUser.name : "Sin usuario"}
      </td>
      <td className="px-6 py-3 whitespace-nowrap">
        {!item.done ? (
          <button
            className="
              bg-blue-500 
              hover:bg-blue-600 
              text-white 
              font-bold 
              py-1 
              px-2 
              rounded 
              text-xs
            "
            onClick={(event) => toggleDone(event, item.id, item.description, !item.done)}
          >
            Done
          </button>
        ) : (
          <button
            className="
              bg-green-500 
              hover:bg-green-600 
              text-white 
              font-bold 
              py-1 
              px-2 
              rounded 
              text-xs
            "
            onClick={(event) => toggleDone(event, item.id, item.description, !item.done)}
          >
            Undo
          </button>
        )}
      </td>
      {item.done && (
        <td className="px-6 py-3 whitespace-nowrap">
          <button
            className="
              bg-red-500 
              hover:bg-red-600 
              text-white 
              font-bold 
              py-1 
              px-2 
              rounded 
              text-xs 
              flex 
              items-center
            "
            onClick={() => deleteItem(item.id)}
          >
            <svg 
              xmlns="http://www.w3.org/2000/svg" 
              className="h-4 w-4 mr-1" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth="2" 
                d="M6 18L18 6M6 6l12 12" 
              />
            </svg>
            Delete
          </button>
        </td>
      )}
    </tr>
  );
}

export default TaskListItem;
