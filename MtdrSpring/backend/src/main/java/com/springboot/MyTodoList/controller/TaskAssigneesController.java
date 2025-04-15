package com.springboot.MyTodoList.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.service.TaskAssigneesService;

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
    
    // REPORTES Tasks 01-06

    // R01: obtener las tareas completadas (Done) para un usuario (ProjectUser) en un sprint específico
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

    // R02: tareas completadas por usuario en semana
    @GetMapping("/user/{projectUserId}/week/{date}/done/count")
    public ResponseEntity<Long> getDoneTasksCountByUserAndWeek(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long count = taskAssigneesService.getCountDoneTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/user/{projectUserId}/week/{date}/done")
    public ResponseEntity<List<TaskAssignees>> getCompletedTasksByUserAndWeek(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R03: tareas completadas por usuario en mes
    @GetMapping("/user/{projectUserId}/month/{date}/done/count")
    public ResponseEntity<Long> getDoneTasksCountByUserAndMonth(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        long count = taskAssigneesService.getCountDoneTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/user/{projectUserId}/month/{date}/done")
    public ResponseEntity<List<TaskAssignees>> getCompletedTasksByUserAndMonth(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R04: tareas completadas por equipo en sprint
    @GetMapping("/team-sprint/{sprintId}/done/count")
    public ResponseEntity<Long> getDoneTasksCountByTeamAndSprint(
            @PathVariable int sprintId) {
        long count = taskAssigneesService.getCountDoneTasksByTeamAndSprint(sprintId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/team-sprint/{sprintId}/done")
    public ResponseEntity<List<TaskAssignees>> getCompletedTasksByTeamAndSprint(
            @PathVariable int sprintId) {
        List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByTeamAndSprint(sprintId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R05: tareas completadas por equipo en semana
    @GetMapping("/team-week/{date}/project/{projectId}/done/count")
    public ResponseEntity<Long> getDoneTasksCountByTeamAndWeek(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long count = taskAssigneesService.getCountDoneTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/team-week/{date}/project/{projectId}/done")
    public ResponseEntity<List<TaskAssignees>> getCompletedTasksByTeamAndWeek(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R06: tareas completadas por equipo en mes
    @GetMapping("/team-month/{date}/project/{projectId}/done/count")
    public ResponseEntity<Long> getDoneTasksCountByTeamAndMonth(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        long count = taskAssigneesService.getCountDoneTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/team-month/{date}/project/{projectId}/done")
    public ResponseEntity<List<TaskAssignees>> getCompletedTasksByTeamAndMonth(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

}
