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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.MyTodoList.model.TimeLogs;
import com.springboot.MyTodoList.service.TimeLogsService;

@RestController
@RequestMapping("/api/timelogs")
public class TimeLogsController {

    @Autowired
    private TimeLogsService timeLogsService;

    // Crear TimeLog
    @PostMapping
    public ResponseEntity<TimeLogs> createTimeLog(@RequestBody TimeLogs timeLog) {
        TimeLogs createdTimeLog = timeLogsService.addTimeLog(timeLog);
        return new ResponseEntity<>(createdTimeLog, HttpStatus.CREATED);
    }

    // Obtener todos los TimeLogs
    @GetMapping
    public ResponseEntity<List<TimeLogs>> getAllTimeLogs() {
        List<TimeLogs> timeLogs = timeLogsService.findAllTimeLogs();
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }

    // Obtener TimeLog por ID
    @GetMapping("/{id}")
    public ResponseEntity<TimeLogs> getTimeLogById(@PathVariable int id) {
        Optional<TimeLogs> timeLog = timeLogsService.getTimeLogById(id);
        return timeLog.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar TimeLog (PUT: reemplazo completo)
    @PutMapping("/{id}")
    public ResponseEntity<TimeLogs> updateTimeLog(@PathVariable int id, @RequestBody TimeLogs timeLogDetails) {
        TimeLogs updatedTimeLog = timeLogsService.updateTimeLog(id, timeLogDetails);
        if (updatedTimeLog != null) {
            return new ResponseEntity<>(updatedTimeLog, HttpStatus.OK);
        }   
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Actualizar parcialmente TimeLog (PATCH)
    @PatchMapping("/{id}")
    public ResponseEntity<TimeLogs> patchTimeLogs(@PathVariable int id, @RequestBody JsonNode timeLogUpdates) {
        TimeLogs patchedTimeLog = timeLogsService.patchTimeLog(id, timeLogUpdates);
        if (patchedTimeLog != null) {
            return new ResponseEntity<>(patchedTimeLog, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Eliminar TimeLog
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeLog(@PathVariable int id) {
        boolean deleted = timeLogsService.deleteTimeLog(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Obtener todos los timeLogs por taskAssignee.
    @GetMapping("/taskassignee/{taskAssigneeId}")
    public ResponseEntity<List<TimeLogs>> getTimeLogsByTaskAssignee(@PathVariable int taskAssigneeId) {
        List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTaskAssignee(taskAssigneeId);
        return new ResponseEntity<>(timeLogs, HttpStatus.OK);
    }

    // REPORTES TimeLogs 07-12

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