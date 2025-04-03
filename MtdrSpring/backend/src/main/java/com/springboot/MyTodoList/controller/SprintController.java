package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

    @Autowired
    private SprintService sprintService;

    // Crear Sprint
    @PostMapping
    public ResponseEntity<Sprint> createSprint(@RequestBody Sprint sprint) {
        Sprint createdSprint = sprintService.addSprint(sprint);
        return new ResponseEntity<>(createdSprint, HttpStatus.CREATED);
    }

    // Obtener todos los Sprints
    @GetMapping
    public ResponseEntity<List<Sprint>> getAllSprints() {
        List<Sprint> sprints = sprintService.findAllSprints();
        return new ResponseEntity<>(sprints, HttpStatus.OK);
    }

    // Obtener Sprint por ID
    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable int id) {
        Optional<Sprint> sprint = sprintService.getSprintById(id);
        return sprint.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                     .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar Sprint (PUT: actualiza todos los campos)
    @PutMapping("/{id}")
    public ResponseEntity<Sprint> updateSprint(@PathVariable int id, @RequestBody Sprint sprintDetails) {
        Sprint updatedSprint = sprintService.updateSprint(id, sprintDetails);
        if (updatedSprint != null) {
            return new ResponseEntity<>(updatedSprint, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Actualizar parcialmente Sprint (PATCH: actualiza solo los campos indicados)
    @PatchMapping("/{id}")
    public ResponseEntity<Sprint> patchSprint(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        Sprint updatedSprint = sprintService.patchSprint(id, updates);
        if (updatedSprint != null) {
            return new ResponseEntity<>(updatedSprint, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Eliminar Sprint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable int id) {
        boolean deleted = sprintService.deleteSprint(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Sprint>> getSprintsByProjectId(@PathVariable("projectId") int projectId) {
        List<Sprint> sprints = sprintService.findSprintsByProjectId(projectId);
        return new ResponseEntity<>(sprints, HttpStatus.OK);
    }
}
