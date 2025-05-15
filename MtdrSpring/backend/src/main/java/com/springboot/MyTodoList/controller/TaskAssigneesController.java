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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.*;

import com.springboot.MyTodoList.dto.TaskAssigneeResponseDTO;
import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.repository.TaskAssigneesRepository;
import com.springboot.MyTodoList.service.TaskAssigneesService;

@RestController
@RequestMapping("/api/task-assignees")
public class TaskAssigneesController {

	@Autowired
	private TaskAssigneesService taskAssigneesService;

	// Crear TaskAssignee (asigna una tarea a un usuario)
	@Operation(summary = "Asignsa una tarea a un usuario", description = "Se asigna un usuario a una tarea")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Asignacion hecha correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping("")
	public ResponseEntity<TaskAssignees> createTaskAssignee(@RequestBody TaskAssignees taskAssignee) {
		TaskAssignees createdTaskAssignee = taskAssigneesService.addTaskAssignee(taskAssignee);
		return new ResponseEntity<>(createdTaskAssignee, HttpStatus.CREATED);
	}

	// Obtener todos los TaskAssignees
	@Operation(summary = "Obtener todas las tareas asignadas", description = "Devuelve una lista con todas las asignaciones de tareas existentes")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping
	public ResponseEntity<List<TaskAssignees>> getAllTaskAssignees() {
		List<TaskAssignees> taskAssignees = taskAssigneesService.getAllTaskAssignees();
		return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
	}

	// Obtener TaskAssignee por ID
	@Operation(summary = "Obtener asignación por ID", description = "Busca y devuelve una asignación de tarea específica según su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Asignación encontrada"),
		@ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/{id}")
	public ResponseEntity<TaskAssignees> getTaskAssigneeById(@PathVariable int id) {
		Optional<TaskAssignees> taskAssignee = taskAssigneesService.getTaskAssigneeById(id);
		return taskAssignee.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar TaskAssignee
	@Operation(summary = "Actualizar asignación de tarea", description = "Actualiza la información de una asignación de tarea")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Asignación actualizada correctamente"),
		@ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PutMapping("/{id}")
	public ResponseEntity<TaskAssignees> updateTaskAssignee(@PathVariable int id,
			@RequestBody TaskAssignees taskAssigneeDetails) {
		TaskAssignees updatedTaskAssignee = taskAssigneesService.updateTaskAssignee(id, taskAssigneeDetails);
		if (updatedTaskAssignee != null) {
			return new ResponseEntity<>(updatedTaskAssignee, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar TaskAssignee
	@Operation(summary = "Eliminar asignación de tarea", description = "Elimina una asignación de tarea por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Asignación eliminada correctamente"),
		@ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTaskAssignee(@PathVariable int id) {
		boolean deleted = taskAssigneesService.deleteTaskAssignee(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Obtener todas las asignaciones de tareas de un usuario (por id del OracleUser)
	@Operation(summary = "Obtener asignaciones por usuario", description = "Obtiene todas las asignaciones de tareas hechas a un usuario usando su ID de OracleUser")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Asignaciones obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<TaskAssignees>> getTaskAssigneesByUser(@PathVariable int userId) {
		List<TaskAssignees> taskAssignees = taskAssigneesService.getTaskAssigneesByUser(userId);
		return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
	}

	@Operation(summary = "Obtener asignaciones por usuario y sprint", description = "Devuelve las asignaciones de un usuario en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Asignaciones obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/sprint/{sprintId}")
	public ResponseEntity<List<TaskAssignees>> getTaskAssigneesByUserAndSprint(@PathVariable int projectUserId,
			@PathVariable int sprintId) {
		List<TaskAssignees> taskAssignees = taskAssigneesService.getTaskAssigneesByUserAndSprint(projectUserId,
				sprintId);
		return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
	}

	@Operation(
		summary = "Obtiener tareas asignadas de un sprint",
		description = "Retorna una lista de tareas asignadas con informacion del usuario y tarea asociada, filtradas por el ID del sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista de tareas asignadas obtenida correctamente",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskAssigneeResponseDTO.class)))),
		@ApiResponse(responseCode = "404", description = "Sprint no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error del servidor")})
	@GetMapping("/by-sprint/{sprintId}")
	public ResponseEntity<List<TaskAssigneeResponseDTO>> getAssigneesBySprintId(@PathVariable int sprintId) {
		List<TaskAssigneeResponseDTO> assignees = taskAssigneesService.getTaskAssigneesBySprintId(sprintId);
		return ResponseEntity.ok(assignees);
	}

	// REPORTES Tasks 01-06

	// R01: obtener las tareas completadas (Done) para un usuario (ProjectUser) en un
	// sprint específico
	@Operation(summary = "Cantidad de tareas completadas por usuario en sprint", description = "Devuelve el total de tareas con estado 'Done' asignadas a un usuario en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/sprint/{sprintId}/done/count")
	public ResponseEntity<Long> getDoneTasksCountByUserAndSprint(@PathVariable int projectUserId,
			@PathVariable int sprintId) {
		long count = taskAssigneesService.getCountDoneTasksByUserAndSprint(projectUserId, sprintId);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	@Operation(summary = "Tareas completadas por usuario en sprint", description = "Lista todas las tareas en estado 'Done' de un usuario en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/sprint/{sprintId}/done")
	public ResponseEntity<List<TaskAssignees>> getCompletedTasksByUserAndSprint(@PathVariable int projectUserId,
			@PathVariable int sprintId) {
		List<TaskAssignees> taskAssignees = taskAssigneesService.getCompletedTasksByUserAndSprint(projectUserId,
				sprintId);
		return new ResponseEntity<>(taskAssignees, HttpStatus.OK);
	}

	@Operation(summary = "Horas reales por usuario en sprint", description = "Devuelve la suma total de horas reales trabajadas por un usuario en un sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/sprint/{sprintId}/real-hours")
	public ResponseEntity<Double> getCompletedTasksStaticRealHoursByUserAndSprint(@PathVariable int projectUserId,
			@PathVariable int sprintId) {
		Double realHours = taskAssigneesService.getStaticRealHoursByUserAndSprint(projectUserId,sprintId);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	// R02: tareas completadas por usuario en semana
	@Operation(summary = "Cantidad de tareas completadas por semana", description = "Devuelve el total de tareas con estado 'Done' de un usuario en una semana (lunes a domingo)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/week/{date}/done/count")
	public ResponseEntity<Long> getDoneTasksCountByUserAndWeek(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		long count = taskAssigneesService.getCountDoneTasksByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	@Operation(summary = "Tareas completadas por semana", description = "Devuelve una lista de tareas en estado 'Done' asignadas a un usuario durante una semana (lunes a domingo)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/week/{date}/done")
	public ResponseEntity<List<TaskAssignees>> getCompletedTasksByUserAndWeek(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@Operation(summary = "Horas reales por semana", description = "Devuelve la cantidad total de horas reales trabajadas por un usuario durante una semana (lunes a domingo)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/week/{date}/real-hours")
	public ResponseEntity<Double> getCompletedTasksStaticRealHoursByUserAndWeek(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		Double realHours = taskAssigneesService.getStaticRealHoursByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	// R03: tareas completadas por usuario en mes
	@Operation(summary = "Cantidad de tareas completadas por mes", description = "Devuelve el número total de tareas con estado 'Done' completadas por un usuario durante un mes")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/month/{date}/done/count")
	public ResponseEntity<Long> getDoneTasksCountByUserAndMonth(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		long count = taskAssigneesService.getCountDoneTasksByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	@Operation(summary = "Tareas completadas por mes", description = "Lista las tareas en estado 'Done' que un usuario completó durante un mes")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/month/{date}/done")
	public ResponseEntity<List<TaskAssignees>> getCompletedTasksByUserAndMonth(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@Operation(summary = "Horas reales por mes", description = "Devuelve la suma total de horas reales trabajadas por un usuario durante un mes")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/user/{projectUserId}/month/{date}/real-hours")
	public ResponseEntity<Double> getCompletedTasksStaticRealHoursByUserAndMonth(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		Double realHours = taskAssigneesService.getStaticRealHoursByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	// R04: tareas completadas por equipo en sprint
	@Operation(summary = "Cantidad de tareas completadas por equipo en sprint", description = "Devuelve el número total de tareas en estado 'Done' completadas por todo el equipo en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-sprint/{sprintId}/done/count")
	public ResponseEntity<Long> getDoneTasksCountByTeamAndSprint(@PathVariable int sprintId) {
		long count = taskAssigneesService.getCountDoneTasksByTeamAndSprint(sprintId);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	@Operation(summary = "Tareas completadas por equipo en sprint", description = "Devuelve todas las tareas en estado 'Done' completadas por el equipo en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-sprint/{sprintId}/done")
	public ResponseEntity<List<TaskAssignees>> getCompletedTasksByTeamAndSprint(@PathVariable int sprintId) {
		List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByTeamAndSprint(sprintId);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@Operation(summary = "Horas reales por equipo en sprint", description = "Devuelve el total de horas reales trabajadas por el equipo en un sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-sprint/{sprintId}/real-hours")
	public ResponseEntity<Double> getCompletedTasksStaticRealHoursByTeamAndSprint(@PathVariable int sprintId) {
		Double realHours = taskAssigneesService.getStaticRealHoursByTeamAndSprint(sprintId);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	// R05: tareas completadas por equipo en semana
	@Operation(summary = "Cantidad de tareas completadas por equipo en semana", description = "Devuelve cuántas tareas en estado 'Done' completó el equipo durante la semana correspondiente a la fecha proporcionada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-week/{date}/project/{projectId}/done/count")
	public ResponseEntity<Long> getDoneTasksCountByTeamAndWeek(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		long count = taskAssigneesService.getCountDoneTasksByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	@Operation(summary = "Tareas completadas por equipo en semana", description = "Lista las tareas en estado 'Done' que el equipo completó durante una semana específica")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-week/{date}/project/{projectId}/done")
	public ResponseEntity<List<TaskAssignees>> getCompletedTasksByTeamAndWeek(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@Operation(summary = "Horas reales por equipo en semana", description = "Devuelve el total de horas reales trabajadas por el equipo durante una semana específica")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-week/{date}/project/{projectId}/real-hours")
	public ResponseEntity<Double> getCompletedTasksStaticRealHoursByTeamAndWeek(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		Double realHours = taskAssigneesService.getStaticRealHoursByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	// R06: tareas completadas por equipo en mes
	@Operation(summary = "Cantidad de tareas completadas por equipo en mes", description = "Devuelve el número total de tareas en estado 'Done' completadas por el equipo durante el mes correspondiente a la fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-month/{date}/project/{projectId}/done/count")
	public ResponseEntity<Long> getDoneTasksCountByTeamAndMonth(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		long count = taskAssigneesService.getCountDoneTasksByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	@Operation(summary = "Tareas completadas por equipo en mes", description = "Lista todas las tareas en estado 'Done' completadas por el equipo durante el mes correspondiente a la fecha proporcionada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-month/{date}/project/{projectId}/done")
	public ResponseEntity<List<TaskAssignees>> getCompletedTasksByTeamAndMonth(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		List<TaskAssignees> tasks = taskAssigneesService.getCompletedTasksByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@Operation(summary = "Horas reales por equipo en mes", description = "Devuelve la suma total de horas reales trabajadas por el equipo durante el mes correspondiente a la fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas obtenidas correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-month/{date}/project/{projectId}/real-hours")
	public ResponseEntity<Double> getCompletedTasksStaticRealHoursByTeamAndMonth(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		Double realHours = taskAssigneesService.getStaticRealHoursByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

}
