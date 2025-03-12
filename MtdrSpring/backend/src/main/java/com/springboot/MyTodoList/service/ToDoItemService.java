package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.repository.ToDoItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ToDoItemService {

    @Autowired
    private ToDoItemRepository toDoItemRepository;

    public List<ToDoItem> findAll(){
        return toDoItemRepository.findAll();
    }

    public ResponseEntity<ToDoItem> getItemById(int id){
        Optional<ToDoItem> todoData = toDoItemRepository.findById(id);
        if (todoData.isPresent()){
            return new ResponseEntity<>(todoData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Crea un nuevo ToDoItem con todos los campos, incluidos deadline, priority y assignedUser.
     */
    public ToDoItem addToDoItem(ToDoItem toDoItem){
        // Aquí podrías agregar validaciones, por ejemplo, verificar si el usuario existe.
        return toDoItemRepository.save(toDoItem);
    }

    /**
     * Elimina un ToDoItem por su ID.
     */
    public boolean deleteToDoItem(int id){
        try {
            toDoItemRepository.deleteById(id);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Actualiza un ToDoItem existente, copiando todos los campos de 'td' al objeto persistido.
     */
   public ToDoItem updateToDoItem(int id, ToDoItem td) {
        Optional<ToDoItem> toDoItemData = toDoItemRepository.findById(id);
        if (toDoItemData.isPresent()) {
            ToDoItem existing = toDoItemData.get();

            // Si la petición trae una descripción, actualizamos
            if (td.getDescription() != null) {
                existing.setDescription(td.getDescription());
            }
            // Si la petición trae un valor de 'done' (por defecto boolean es false/true),
            // asumimos que se está intentando actualizarlo:
            existing.setDone(td.isDone());

            // Si la petición trae creation_ts, lo actualizamos
            // (a veces no se desea cambiar la fecha de creación, decide según tu caso)
            if (td.getCreation_ts() != null) {
                existing.setCreation_ts(td.getCreation_ts());
            }

            // Deadline
            if (td.getDeadline() != null) {
                existing.setDeadline(td.getDeadline());
            }

            // Priority
            if (td.getPriority() != null) {
                existing.setPriority(td.getPriority());
            }

            // Usuario asignado (solo si no es null)
            if (td.getAssignedUser() != null && td.getAssignedUser().getIdUser() != 0) {
                existing.setAssignedUser(td.getAssignedUser());
            }

            // Asignamos el mismo ID para que no se pierda la referencia
            existing.setID(id);

            return toDoItemRepository.save(existing);
        } else {
            return null;
        }
    }
        // ToDoItemService.java
        public List<ToDoItem> getItemsByUserId(int userId) {
            return toDoItemRepository.findByAssignedUserIdUser(userId);
        }

}
