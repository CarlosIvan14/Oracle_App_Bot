package com.springboot.MyTodoList.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

}