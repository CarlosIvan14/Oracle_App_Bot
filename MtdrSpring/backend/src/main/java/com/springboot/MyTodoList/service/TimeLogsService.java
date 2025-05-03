package com.springboot.MyTodoList.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.TimeLogs;
import com.springboot.MyTodoList.repository.TimeLogsRepository;

@Service
public class TimeLogsService {

	@Autowired
	private TimeLogsRepository timeLogsRepository;

	public TimeLogs addTimeLog(TimeLogs timeLogs) {
		return timeLogsRepository.save(timeLogs);
	}

	// Is this one really usefull in here?
	public List<TimeLogs> findAllTimeLogs() {
		return timeLogsRepository.findAll();
	}

	public Optional<TimeLogs> getTimeLogById(int id) {
		return timeLogsRepository.findById(id);
	}

	public TimeLogs updateTimeLog(int id, TimeLogs timeLogDetails) {
		return timeLogsRepository.findById(id).map(timeLog -> {
			// Actualización total (PUT)
			timeLog.setStartTs(timeLogDetails.getStartTs());
			timeLog.setEndTs(timeLogDetails.getEndTs());

			// Guardar los cambios en el repositorio
			return timeLogsRepository.save(timeLog);
		}).orElse(null);
	}

	public TimeLogs patchTimeLog(int id, JsonNode timeLogUpdates) {
		return timeLogsRepository.findById(id).map(timeLog -> {
			ObjectMapper mapper = new ObjectMapper();
			// Para evitar fallos si se envían propiedades desconocidas
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				// Fusiona (merge) los campos enviados en taskUpdates sobre el objeto task
				// existente
				mapper.readerForUpdating(timeLog).readValue(timeLogUpdates);
			}
			catch (IOException e) {
				throw new RuntimeException("Error al actualizar el timelog", e);
			}
			return timeLogsRepository.save(timeLog);
		}).orElse(null);
	}

	public boolean deleteTimeLog(int id) {
		if (timeLogsRepository.existsById(id)) {
			timeLogsRepository.deleteById(id);
			return true;
		}
		return false;
	}

	// Método para obtener todos los timeLogs de un taskAssignee.
	public List<TimeLogs> getTimeLogsByTaskAssignee(int taskAssigneeId) {
		return timeLogsRepository.findByTaskAssigneeID(taskAssigneeId);
	}

	// Método para obtener todos los timeLogs de un arreglo de taskAssignees.
	public List<TimeLogs> getTimeLogsByTaskAssignees(List<Integer> taskAssigneeIdArray) {
		return timeLogsRepository.findByTaskAssigneeIds(taskAssigneeIdArray);
	}

	// Método para obtener el realtime en conjunto de un arreglo de taskAssignees.
	public Long getRealTimeSumByTaskAssignees(List<Integer> taskAssigneeIdArray) {
		List<TimeLogs> timeLogs = timeLogsRepository.findByTaskAssigneeIds(taskAssigneeIdArray);

		return timeLogs.stream().map(timeLog -> {
			if (timeLog.getStartTs() != null && timeLog.getEndTs() != null) {
				return java.time.Duration.between(timeLog.getStartTs(), timeLog.getEndTs()).toHours();
			}
			else {
				return 0L;
			}
		}).reduce(0L, Long::sum);
	}

	// HELPER
	public long getRealHoursByTimeLogs(List<TimeLogs> timeLogs) {
		return timeLogs.stream().map(timeLog -> {
			if (timeLog.getStartTs() != null && timeLog.getEndTs() != null) {
				return java.time.Duration.between(timeLog.getStartTs(), timeLog.getEndTs()).toHours();
			}
			else {
				return 0L;
			}
		}).reduce(0L, Long::sum);
	}

	// timelogs-user-sprint methods (R07)
	public long getRealHoursByUserAndSprint(int projectUserId, int sprintId) {
		List<TimeLogs> timeLogs = timeLogsRepository.findTimeLogsByProjectUserAndSprint(projectUserId, sprintId);
		return getRealHoursByTimeLogs(timeLogs);
	}

	public List<TimeLogs> getTimeLogsByUserAndSprint(int projectUserId, int sprintId) {
		return timeLogsRepository.findTimeLogsByProjectUserAndSprint(projectUserId, sprintId);
	}

	// timelogs-user-daterange methods (R08 y R09)
	public long getRealHoursByUserByDateRange(int projectUserId, LocalDate from, LocalDate to) {
		List<TimeLogs> timeLogs = timeLogsRepository.findTimeLogsByProjectUserAndDateRange(projectUserId,
				from.atStartOfDay(), to.plusDays(1).atStartOfDay());
		return getRealHoursByTimeLogs(timeLogs);
	}

	public List<TimeLogs> getTimeLogsByUserByDateRange(int projectUserId, LocalDate from, LocalDate to) {
		return timeLogsRepository.findTimeLogsByProjectUserAndDateRange(projectUserId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
	}

	// timelogs-team-sprint methods (R10)
	public long getRealHoursByTeamAndSprint(int sprintId) {
		List<TimeLogs> timeLogs = timeLogsRepository.findTimeLogsByTeamAndSprint(sprintId);
		return getRealHoursByTimeLogs(timeLogs);
	}

	public List<TimeLogs> getTimeLogsByTeamAndSprint(int sprintId) {
		return timeLogsRepository.findTimeLogsByTeamAndSprint(sprintId);
	}

	// timelogs-team-daterange methods (R11 y R12)
	public long getRealHoursByTeamByDateRange(int projectId, LocalDate from, LocalDate to) {
		List<TimeLogs> timeLogs = timeLogsRepository.findTimeLogsByTeamAndDateRange(projectId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
		return getRealHoursByTimeLogs(timeLogs);
	}

	public List<TimeLogs> getTimeLogsByTeamByDateRange(int projectId, LocalDate from, LocalDate to) {
		return timeLogsRepository.findTimeLogsByTeamAndDateRange(projectId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
	}

}