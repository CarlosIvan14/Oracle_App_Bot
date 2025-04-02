package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.service.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TasksController {

    @Autowired
    private TasksService tasksService;

    // Crear Task
    @PostMapping
    public ResponseEntity<Tasks> createTask(@RequestBody Tasks task) {
        Tasks createdTask = tasksService.addTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // Obtener todas las Tasks
    @GetMapping
    public ResponseEntity<List<Tasks>> getAllTasks() {
        List<Tasks> tasks = tasksService.findAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // Obtener Task por ID
    @GetMapping("/{id}")
    public ResponseEntity<Tasks> getTaskById(@PathVariable int id) {
        Optional<Tasks> task = tasksService.getTaskById(id);
        return task.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar Task
    @PutMapping("/{id}")
    public ResponseEntity<Tasks> updateTask(@PathVariable int id, @RequestBody Tasks taskDetails) {
        Tasks updatedTask = tasksService.updateTask(id, taskDetails);
        if (updatedTask != null) {
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Eliminar Task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id) {
        boolean deleted = tasksService.deleteTask(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
