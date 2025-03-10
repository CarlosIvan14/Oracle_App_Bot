import React, { useState } from 'react';

function NewItem({ addItem, isInserting }) {
  const [description, setDescription] = useState('');
  const [deadline, setDeadline] = useState('');
  const [priority, setPriority] = useState('');
  const [assignedUserId, setAssignedUserId] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!description || !deadline || !priority || !assignedUserId) {
      alert("Por favor, completa todos los campos");
      return;
    }
    // Se crea el objeto con los nuevos campos
    const newTask = {
      description,
      deadline, // debe estar en formato "yyyy-MM-dd"
      priority: parseInt(priority, 10),
      assignedUser: { id: parseInt(assignedUserId, 10) }
    };
    addItem(newTask);
    // Limpiar el formulario
    setDescription('');
    setDeadline('');
    setPriority('');
    setAssignedUserId('');
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        className='rounded-full'
        type="text"
        placeholder="Descripción"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />
      <input
        type="date"
        placeholder="Fecha Límite"
        value={deadline}
        onChange={(e) => setDeadline(e.target.value)}
      />
      <select
        value={priority}
        onChange={(e) => setPriority(e.target.value)}
      >
        <option value="">Prioridad</option>
        <option value="1">Alta</option>
        <option value="2">Media</option>
        <option value="3">Baja</option>
      </select>
      <input
        type="number"
        placeholder="ID Usuario"
        value={assignedUserId}
        onChange={(e) => setAssignedUserId(e.target.value)}
      />
      <button 
        type="submit" 
        className="AddButton"
        disabled={isInserting}
      >
        {isInserting ? "Agregando..." : "Agregar Tarea"}
      </button>
    </form>
  );
}

export default NewItem;
