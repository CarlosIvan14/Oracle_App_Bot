package com.springboot.MyTodoList.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.model.TimeLogs;
import com.springboot.MyTodoList.service.TaskAssigneesService;
import com.springboot.MyTodoList.service.TimeLogsService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private TaskAssigneesService taskAssigneesService;
    @Autowired
    private TimeLogsService timeLogsService;


    // R01: obtener las tareas completadas (Done) para un usuario (ProjectUser) en un sprint específico
    @GetMapping("/individual-sprint/{sprintId}/{projectUserId}/tasks-done/count")
    public ResponseEntity<Long> getDoneTasksCountByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        long count = taskAssigneesService.getCountDoneTasksByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/individual-sprint/{sprintId}/{projectUserId}/tasks-done")
    public ResponseEntity<List<Tasks>> getCompletedTasksByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        List<Tasks> taskAssignees = taskAssigneesService.getCompletedTasksByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
    }

    // R02: tareas completadas por usuario en semana
    @GetMapping("/individual-week/{date}/{projectUserId}/tasks-done/count")
    public ResponseEntity<Long> getDoneTasksCountByUserAndWeek(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long count = taskAssigneesService.getCountDoneTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/individual-week/{date}/{projectUserId}/tasks-done")
    public ResponseEntity<List<Tasks>> getCompletedTasksByUserAndWeek(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Tasks> tasks = taskAssigneesService.getCompletedTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R03: tareas completadas por usuario en mes
    @GetMapping("/individual-month/{date}/{projectUserId}/tasks-done/count")
    public ResponseEntity<Long> getDoneTasksCountByUserAndMonth(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        long count = taskAssigneesService.getCountDoneTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/individual-month/{date}/{projectUserId}/tasks-done")
    public ResponseEntity<List<Tasks>> getCompletedTasksByUserAndMonth(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        List<Tasks> tasks = taskAssigneesService.getCompletedTasksByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R04: tareas completadas por equipo en sprint
    @GetMapping("/team-sprint/{sprintId}/tasks-done/count")
    public ResponseEntity<Long> getDoneTasksCountByTeamAndSprint(
            @PathVariable int sprintId) {
        long count = taskAssigneesService.getCountDoneTasksByTeamAndSprint(sprintId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/team-sprint/{sprintId}/tasks-done")
    public ResponseEntity<List<Tasks>> getCompletedTasksByTeamAndSprint(
            @PathVariable int sprintId) {
        List<Tasks> tasks = taskAssigneesService.getCompletedTasksByTeamAndSprint(sprintId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R05: tareas completadas por equipo en semana
    @GetMapping("/team-week/{date}/{projectId}/tasks-done/count")
    public ResponseEntity<Long> getDoneTasksCountByTeamAndWeek(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long count = taskAssigneesService.getCountDoneTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/team-week/{date}/{projectId}/tasks-done")
    public ResponseEntity<List<Tasks>> getCompletedTasksByTeamAndWeek(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Tasks> tasks = taskAssigneesService.getCompletedTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // R06: tareas completadas por equipo en mes
    @GetMapping("/team-month/{date}/{projectId}/tasks-done/count")
    public ResponseEntity<Long> getDoneTasksCountByTeamAndMonth(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        long count = taskAssigneesService.getCountDoneTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/team-month/{date}/{projectId}/tasks-done")
    public ResponseEntity<List<Tasks>> getCompletedTasksByTeamAndMonth(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        List<Tasks> tasks = taskAssigneesService.getCompletedTasksByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // TODO: Endpoints from R07-R12

    // R07: horas reales por usuario en sprint
    @GetMapping("/individual-sprint/{sprintId}/{projectUserId}/real-hours")
    public ResponseEntity<Long> getRealHoursByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        long realHours = timeLogsService.getRealHoursByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(realHours, HttpStatus.OK);
    }

    @GetMapping("/individual-sprint/{sprintId}/{projectUserId}/time-logs")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByUserAndSprint(
            @PathVariable int projectUserId, @PathVariable int sprintId) {
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByUserAndSprint(projectUserId, sprintId);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }
                
    // R08: horas reales por usuario en semana
    @GetMapping("/individual-week/{date}/{projectUserId}/real-hours")
    public ResponseEntity<Long> getRealHoursByUserAndWeek(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long realHours = timeLogsService.getRealHoursByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(realHours, HttpStatus.OK);
    }

    @GetMapping("/individual-week/{date}/{projectUserId}/time-logs")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByUserAndWeek(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }

    // R09: horas reales por usuario en mes
    @GetMapping("/individual-month/{date}/{projectUserId}/real-hours")
    public ResponseEntity<Long> getRealHoursByUserAndMonth(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        long realHours = timeLogsService.getRealHoursByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(realHours, HttpStatus.OK);
    }

    @GetMapping("/individual-month/{date}/{projectUserId}/time-logs")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByUserAndMonth(
            @PathVariable int projectUserId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByUserByDateRange(projectUserId, from, to);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }

    // R10: horas reales por equipo en sprint
    @GetMapping("/team-sprint/{sprintId}/real-hours")
    public ResponseEntity<Long> getRealHoursByTeamAndSprint(
            @PathVariable int sprintId) {
        long realHours = timeLogsService.getRealHoursByTeamAndSprint(sprintId);
        return new ResponseEntity<>(realHours, HttpStatus.OK);
    }

    @GetMapping("/team-sprint/{sprintId}/time-logs")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByTeamAndSprint(
            @PathVariable int sprintId) {
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTeamAndSprint(sprintId);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }

    // // R11: horas reales por equipo en semana
    @GetMapping("/team-week/{date}/{projectId}/real-hours")
    public ResponseEntity<Long> getRealHoursByTeamAndWeek(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long realHours = timeLogsService.getRealHoursByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(realHours, HttpStatus.OK);
    }

    @GetMapping("/team-week/{date}/{projectId}/time-logs")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByTeamAndWeek(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }

    // R12: horas reales por equipo en mes
    @GetMapping("/team-month/{date}/{projectId}/real-hours")
    public ResponseEntity<Long> getRealHoursByTeamAndMonth(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        long realHours = timeLogsService.getRealHoursByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(realHours, HttpStatus.OK);
    }

    @GetMapping("/team-month/{date}/{projectId}/tasks-done")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByTeamAndMonth(
            @PathVariable int projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTeamByDateRange(projectId, from, to);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }
}
