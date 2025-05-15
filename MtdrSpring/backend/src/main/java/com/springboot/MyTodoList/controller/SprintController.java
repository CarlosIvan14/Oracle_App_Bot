package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

	@Autowired
	private SprintService sprintService;

	// Crear Sprint
	@Operation(summary = "Crear sprint", description = "Crea un nuevo sprint con los datos proporcionados")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Sprint creado correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping
	public ResponseEntity<Sprint> createSprint(@RequestBody Sprint sprint) {
		Sprint createdSprint = sprintService.addSprint(sprint);
		return new ResponseEntity<>(createdSprint, HttpStatus.CREATED);
	}

	// Obtener todos los Sprints
	@Operation(summary = "Obtener todos los sprints", description = "Devuelve una lista de todos los sprints")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping
	public ResponseEntity<List<Sprint>> getAllSprints() {
		List<Sprint> sprints = sprintService.findAllSprints();
		return new ResponseEntity<>(sprints, HttpStatus.OK);
	}

	// Obtener Sprint por ID
	@Operation(summary = "Obtener sprint por ID", description = "Devuelve un sprint específico por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sprint encontrado"),
		@ApiResponse(responseCode = "404", description = "Sprint no encontrado")
	})
	@GetMapping("/{id}")
	public ResponseEntity<Sprint> getSprintById(@PathVariable int id) {
		Optional<Sprint> sprint = sprintService.getSprintById(id);
		return sprint.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar Sprint (PUT: actualiza todos los campos)
	@Operation(summary = "Actualizar sprint", description = "Reemplaza completamente los datos de un sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sprint actualizado correctamente"),
		@ApiResponse(responseCode = "404", description = "Sprint no encontrado")
	})
	@PutMapping("/{id}")
	public ResponseEntity<Sprint> updateSprint(@PathVariable int id, @RequestBody Sprint sprintDetails) {
		Sprint updatedSprint = sprintService.updateSprint(id, sprintDetails);
		if (updatedSprint != null) {
			return new ResponseEntity<>(updatedSprint, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Actualizar parcialmente Sprint (PATCH: actualiza solo los campos indicados)
	@Operation(summary = "Actualizar parcialmente sprint", description = "Modifica uno o más campos específicos de un sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sprint actualizado correctamente"),
		@ApiResponse(responseCode = "404", description = "Sprint no encontrado")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<Sprint> patchSprint(@PathVariable int id, @RequestBody Map<String, Object> updates) {
		Sprint updatedSprint = sprintService.patchSprint(id, updates);
		if (updatedSprint != null) {
			return new ResponseEntity<>(updatedSprint, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar Sprint
	@Operation(summary = "Eliminar sprint", description = "Elimina un sprint existente por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Sprint eliminado correctamente"),
		@ApiResponse(responseCode = "404", description = "Sprint no encontrado")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSprint(@PathVariable int id) {
		boolean deleted = sprintService.deleteSprint(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Obtener sprints por proyecto", description = "Devuelve todos los sprints asociados a un proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista de sprints obtenida correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
	})
	@GetMapping("/project/{projectId}")
	public ResponseEntity<List<Sprint>> getSprintsByProjectId(@PathVariable("projectId") int projectId) {
		List<Sprint> sprints = sprintService.findSprintsByProjectId(projectId);
		return new ResponseEntity<>(sprints, HttpStatus.OK);
	}

}
