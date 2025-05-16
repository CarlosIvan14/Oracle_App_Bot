// src/components/TaskModal.js
import React from "react";

function TaskModal({ task, onClose }) {
  if (!task) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-[#212233] text-white p-8 rounded-3xl relative max-w-md w-full">
        <button
          className="absolute top-4 right-8 text-gray-400 hover:text-white"
          onClick={onClose}
        >
          X
        </button>
        <div className="flex justify-center">
          <h2 className="text-2xl font-bold mb-2">{task.title}</h2>
        </div>
        <p className="mb-2">Asignado a: {task.assignee}</p>
        <p className="mb-2">Estado: {task.status}</p>
        <p className="mb-4">Descripción: {task.description}</p>
        <div className="flex justify-center">
          <button
            onClick={onClose}
            className="bg-red-600 px-4 py-2 rounded-full hover:bg-red-700"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}

export default TaskModal;
