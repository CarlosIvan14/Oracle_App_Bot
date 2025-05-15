// TasksController.java
package com.springboot.MyTodoList.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.MyTodoList.dto.SimplifiedTaskDTO;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.service.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TasksController {

	@Autowired
	private TasksService tasksService;

	// Crear Task
	@Operation(summary = "Crear nueva tarea", description = "Crea una nueva tarea en el sistema")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping
	public ResponseEntity<Tasks> createTask(@RequestBody Tasks task) {
		Tasks createdTask = tasksService.addTask(task);
		return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
	}

	// Obtener todas las Tasks
	@Operation(summary = "Obtener todas las tareas", description = "Devuelve una lista de todas las tareas registradas")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping
	public ResponseEntity<List<Tasks>> getAllTasks() {
		List<Tasks> tasks = tasksService.findAllTasks();
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	// Obtener Task por ID
	@Operation(summary = "Obtener tarea por ID", description = "Devuelve los detalles de una tarea específica según su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea encontrada correctamente"),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/{id}")
	public ResponseEntity<Tasks> getTaskById(@PathVariable int id) {
		Optional<Tasks> task = tasksService.getTaskById(id);
		return task.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar Task (PUT: reemplazo completo)
	@Operation(summary = "Actualizar tarea (reemplazo total)", description = "Reemplaza por completo los datos de una tarea existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea actualizada correctamente"),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PutMapping("/{id}")
	public ResponseEntity<Tasks> updateTask(@PathVariable int id, @RequestBody Tasks taskDetails) {
		Tasks updatedTask = tasksService.updateTask(id, taskDetails);
		if (updatedTask != null) {
			return new ResponseEntity<>(updatedTask, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Actualizar parcialmente Task (PATCH)
	@Operation(summary = "Actualizar tarea parcialmente", description = "Modifica uno o más campos de una tarea existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea actualizada parcialmente"),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<Tasks> patchTask(@PathVariable int id, @RequestBody JsonNode taskUpdates) {
		Tasks patchedTask = tasksService.patchTask(id, taskUpdates);
		if (patchedTask != null) {
			return new ResponseEntity<>(patchedTask, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar Task
	@Operation(summary = "Eliminar tarea", description = "Elimina una tarea existente por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Tarea eliminada correctamente"),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTask(@PathVariable int id) {
		boolean deleted = tasksService.deleteTask(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Obtener todas las tareas de un Sprint por id_sprint
	@Operation(summary = "Obtener tareas por sprint", description = "Devuelve una lista de tareas asociadas a un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/sprint/{sprintId}")
	public ResponseEntity<List<Tasks>> getTasksBySprint(@PathVariable int sprintId) {
		List<Tasks> tasks = tasksService.getTasksBySprint(sprintId);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	// Get unassigned tasks by sprint ID
	@Operation(
		summary = "Obtener tareas no asignadas por Sprint",
		description = "Retorna una lista de tareas que no han sido asignadas dentro del sprint especificado")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas no asignadas obtenidas correctamente",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = SimplifiedTaskDTO.class)))),
		@ApiResponse(responseCode = "404", description = "Sprint no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error del servidor")})
	@GetMapping("/unassigned/{sprintId}")
	public ResponseEntity<List<SimplifiedTaskDTO>> getSimpleUnassignedTasksBySprint(@PathVariable int sprintId) {
		List<Tasks> tasks = tasksService.getUnassignedTasksBySprint(sprintId);

		List<SimplifiedTaskDTO> result = tasks.stream()
				.map(task -> new SimplifiedTaskDTO(task.getId(), task.getName(), task.getStatus(),
						task.getDescription(), task.getStoryPoints(), task.getDeadline(), task.getEstimatedHours()))
				.collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

}