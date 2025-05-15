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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/timelogs")
public class TimeLogsController {

	@Autowired
	private TimeLogsService timeLogsService;

	// Crear TimeLog
	@Operation(summary = "Crear nuevo registro de tiempo", description = "Crea un nuevo registro de tiempo asociado a una tarea")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Registro creado exitosamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping
	public ResponseEntity<TimeLogs> createTimeLog(@RequestBody TimeLogs timeLog) {
		TimeLogs createdTimeLog = timeLogsService.addTimeLog(timeLog);
		return new ResponseEntity<>(createdTimeLog, HttpStatus.CREATED);
	}

	// Obtener todos los TimeLogs
	@Operation(summary = "Obtener todos los registros de tiempo", description = "Devuelve una lista con todos los registros de tiempo")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente")
	})
	@GetMapping
	public ResponseEntity<List<TimeLogs>> getAllTimeLogs() {
		List<TimeLogs> timeLogs = timeLogsService.findAllTimeLogs();
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// Obtener TimeLog por ID
	@Operation(summary = "Obtener registro de tiempo por ID", description = "Devuelve un registro de tiempo específico según su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registro encontrado"),
		@ApiResponse(responseCode = "404", description = "Registro no encontrado")
	})
	@GetMapping("/{id}")
	public ResponseEntity<TimeLogs> getTimeLogById(@PathVariable int id) {
		Optional<TimeLogs> timeLog = timeLogsService.getTimeLogById(id);
		return timeLog.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar TimeLog (PUT: reemplazo completo)
	@Operation(summary = "Actualizar registro de tiempo", description = "Actualiza completamente un registro de tiempo existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registro actualizado correctamente"),
		@ApiResponse(responseCode = "404", description = "Registro no encontrado")
	})
	@PutMapping("/{id}")
	public ResponseEntity<TimeLogs> updateTimeLog(@PathVariable int id, @RequestBody TimeLogs timeLogDetails) {
		TimeLogs updatedTimeLog = timeLogsService.updateTimeLog(id, timeLogDetails);
		if (updatedTimeLog != null) {
			return new ResponseEntity<>(updatedTimeLog, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Actualizar parcialmente TimeLog (PATCH)
	@Operation(summary = "Actualizar parcialmente un registro de tiempo", description = "Actualiza campos específicos de un registro de tiempo")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registro actualizado correctamente"),
		@ApiResponse(responseCode = "404", description = "Registro no encontrado")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<TimeLogs> patchTimeLogs(@PathVariable int id, @RequestBody JsonNode timeLogUpdates) {
		TimeLogs patchedTimeLog = timeLogsService.patchTimeLog(id, timeLogUpdates);
		if (patchedTimeLog != null) {
			return new ResponseEntity<>(patchedTimeLog, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar TimeLog
	@Operation(summary = "Eliminar registro de tiempo", description = "Elimina un registro de tiempo por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Registro eliminado correctamente"),
		@ApiResponse(responseCode = "404", description = "Registro no encontrado")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTimeLog(@PathVariable int id) {
		boolean deleted = timeLogsService.deleteTimeLog(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Obtener todos los timeLogs por taskAssignee.
	@Operation(summary = "Obtener registros por asignación de tarea", description = "Devuelve todos los registros de tiempo asociados a un TaskAssignee")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente")
	})
	@GetMapping("/taskassignee/{taskAssigneeId}")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByTaskAssignee(@PathVariable int taskAssigneeId) {
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTaskAssignee(taskAssigneeId);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// REPORTES TimeLogs 07-12

	// R07: horas reales por usuario en sprint
	@Operation(summary = "Horas reales por usuario en sprint", description = "Obtiene la suma de horas reales registradas por un usuario en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas reales obtenidas correctamente")
	})
	@GetMapping("/individual-sprint/{sprintId}/{projectUserId}/real-hours")
	public ResponseEntity<Long> getRealHoursByUserAndSprint(@PathVariable int projectUserId,
			@PathVariable int sprintId) {
		long realHours = timeLogsService.getRealHoursByUserAndSprint(projectUserId, sprintId);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	@Operation(summary = "Registros de tiempo por usuario en sprint", description = "Devuelve todos los registros de tiempo de un usuario en un sprint específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente")
	})
	@GetMapping("/individual-sprint/{sprintId}/{projectUserId}/time-logs")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByUserAndSprint(@PathVariable int projectUserId,
			@PathVariable int sprintId) {
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByUserAndSprint(projectUserId, sprintId);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// R08: horas reales por usuario en semana
	@Operation(summary = "Horas reales por usuario en semana", description = "Obtiene la suma de horas reales registradas por un usuario en la semana de una fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas reales obtenidas correctamente")
	})
	@GetMapping("/individual-week/{date}/{projectUserId}/real-hours")
	public ResponseEntity<Long> getRealHoursByUserAndWeek(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		long realHours = timeLogsService.getRealHoursByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}


	@Operation(summary = "Registros de tiempo por usuario en semana", description = "Devuelve todos los registros de tiempo de un usuario en la semana de una fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente")
	})
	@GetMapping("/individual-week/{date}/{projectUserId}/time-logs")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByUserAndWeek(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// R09: horas reales por usuario en mes
	@Operation(summary = "Horas reales por usuario en mes", description = "Obtiene la suma de horas reales registradas por un usuario en un mes específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas reales obtenidas correctamente")
	})
	@GetMapping("/individual-month/{date}/{projectUserId}/real-hours")
	public ResponseEntity<Long> getRealHoursByUserAndMonth(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		long realHours = timeLogsService.getRealHoursByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	@Operation(summary = "Registros de tiempo por usuario en mes", description = "Devuelve todos los registros de tiempo de un usuario durante un mes específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente")
	})
	@GetMapping("/individual-month/{date}/{projectUserId}/time-logs")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByUserAndMonth(@PathVariable int projectUserId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByUserByDateRange(projectUserId, from, to);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// R10: horas reales por equipo en sprint
	@Operation(summary = "Horas reales por equipo en sprint", description = "Obtiene la suma de horas reales registradas por todos los miembros del equipo en un sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas reales obtenidas correctamente")
	})
	@GetMapping("/team-sprint/{sprintId}/real-hours")
	public ResponseEntity<Long> getRealHoursByTeamAndSprint(@PathVariable int sprintId) {
		long realHours = timeLogsService.getRealHoursByTeamAndSprint(sprintId);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	@Operation(summary = "Registros de tiempo por equipo en sprint", description = "Devuelve todos los registros de tiempo de un equipo durante un sprint")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente")
	})
	@GetMapping("/team-sprint/{sprintId}/time-logs")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByTeamAndSprint(@PathVariable int sprintId) {
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTeamAndSprint(sprintId);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// // R11: horas reales por equipo en semana
	@Operation(summary = "Horas reales por equipo en semana", description = "Obtiene la suma de horas reales registradas por todos los miembros del equipo durante la semana de una fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas reales obtenidas correctamente")
	})
	@GetMapping("/team-week/{date}/{projectId}/real-hours")
	public ResponseEntity<Long> getRealHoursByTeamAndWeek(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		long realHours = timeLogsService.getRealHoursByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	@Operation(summary = "Obtener registros de tiempo por semana del equipo", description = "Devuelve todos los registros de tiempo de un equipo durante la semana de una fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-week/{date}/{projectId}/time-logs")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByTeamAndWeek(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

	// R12: horas reales por equipo en mes
	@Operation(summary = "Obtener horas reales por equipo en el mes", description = "Obtiene la suma de horas reales registradas por el equipo en el mes de una fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Horas reales obtenidas correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-month/{date}/{projectId}/real-hours")
	public ResponseEntity<Long> getRealHoursByTeamAndMonth(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		long realHours = timeLogsService.getRealHoursByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(realHours, HttpStatus.OK);
	}

	@Operation(summary = "Obtener registros de tiempo por mes del equipo", description = "Devuelve todos los registros de tiempo del equipo correspondientes al mes de una fecha dada")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Registros obtenidos correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/team-month/{date}/{projectId}/tasks-done")
	public ResponseEntity<List<TimeLogs>> getTimeLogsByTeamAndMonth(@PathVariable int projectId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		LocalDate from = date.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate to = date.with(TemporalAdjusters.lastDayOfMonth());
		List<TimeLogs> timeLogs = timeLogsService.getTimeLogsByTeamByDateRange(projectId, from, to);
		return new ResponseEntity<>(timeLogs, HttpStatus.OK);
	}

}