package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.service.TaskAssigneesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/task-assignees")
public class TaskAssigneesController {

    @Autowired
    private TaskAssigneesService taskAssigneesService;

    // Crear TaskAssignee (asigna una tarea a un usuario)
    @PostMapping
    public ResponseEntity<TaskAssignees> createTaskAssignee(@RequestBody TaskAssignees taskAssignee) {
        TaskAssignees createdTaskAssignee = taskAssigneesService.addTaskAssignee(taskAssignee);
        return new ResponseEntity<>(createdTaskAssignee, HttpStatus.CREATED);
    }

    // Obtener todos los TaskAssignees
    @GetMapping
    public ResponseEntity<List<TaskAssignees>> getAllTaskAssignees() {
        List<TaskAssignees> taskAssignees = taskAssigneesService.getAllTaskAssignees();
        return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
    }

    // Obtener TaskAssignee por ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskAssignees> getTaskAssigneeById(@PathVariable int id) {
        Optional<TaskAssignees> taskAssignee = taskAssigneesService.getTaskAssigneeById(id);
        return taskAssignee.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                           .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar TaskAssignee
    @PutMapping("/{id}")
    public ResponseEntity<TaskAssignees> updateTaskAssignee(@PathVariable int id, @RequestBody TaskAssignees taskAssigneeDetails) {
        TaskAssignees updatedTaskAssignee = taskAssigneesService.updateTaskAssignee(id, taskAssigneeDetails);
        if (updatedTaskAssignee != null) {
            return new ResponseEntity<>(updatedTaskAssignee, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Eliminar TaskAssignee
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskAssignee(@PathVariable int id) {
        boolean deleted = taskAssigneesService.deleteTaskAssignee(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Obtener todas las asignaciones de tareas de un usuario (por id del OracleUser)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskAssignees>> getTaskAssigneesByUser(@PathVariable int userId) {
        List<TaskAssignees> taskAssignees = taskAssigneesService.getTaskAssigneesByUser(userId);
        return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
    }
    @GetMapping("/user/{projectUserId}/sprint/{sprintId}")
    public ResponseEntity<List<TaskAssignees>> getTaskAssigneesByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        List<TaskAssignees> taskAssignees = taskAssigneesService.getTaskAssigneesByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
    }
    // Nuevo endpoint: obtener la cantidad de tareas "Done" para un usuario (ProjectUser) en un sprint específico
    @GetMapping("/user/{projectUserId}/sprint/{sprintId}/done/count")
    public ResponseEntity<Long> getDoneTasksCountByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        long count = taskAssigneesService.getCountDoneTasksByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
    @GetMapping("/user/{projectUserId}/sprint/{sprintId}/done")
    public ResponseEntity<List<TaskAssignees>> getCompletedTasksByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        List<TaskAssignees> taskAssignees = taskAssigneesService.getCompletedTasksByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
    }

}
