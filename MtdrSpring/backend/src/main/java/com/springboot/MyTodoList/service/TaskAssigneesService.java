package com.springboot.MyTodoList.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.dto.ProjectUserDTO;
import com.springboot.MyTodoList.dto.TaskAssigneeResponseDTO;
import com.springboot.MyTodoList.dto.TaskDTO;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.repository.ProjectUserRepository;
import com.springboot.MyTodoList.repository.TaskAssigneesRepository;
import com.springboot.MyTodoList.repository.TasksRepository;

@Service
public class TaskAssigneesService {

	@Autowired
	private TaskAssigneesRepository taskAssigneesRepository;

	@Autowired
	private TasksRepository tasksRepository;

	@Autowired
	private ProjectUserRepository projectUserRepository;

	// Método para agregar una nueva asignación (valida que el Task y el ProjectUser
	// existan)
	public TaskAssignees addTaskAssignee(TaskAssignees taskAssignee) {
		// Validar Task
		if (taskAssignee.getTask() != null && taskAssignee.getTask().getId() != 0) {
			Optional<Tasks> existingTask = tasksRepository.findById(taskAssignee.getTask().getId());
			if (existingTask.isPresent()) {
				taskAssignee.setTask(existingTask.get());
			}
			else {
				throw new IllegalArgumentException("Task with id " + taskAssignee.getTask().getId() + " not found.");
			}
		}
		else {
			throw new IllegalArgumentException("Task must be provided with a valid id.");
		}

		// Validar ProjectUser
		if (taskAssignee.getProjectUser() != null && taskAssignee.getProjectUser().getIdProjectUser() != 0) {
			Optional<ProjectUser> existingProjectUser = projectUserRepository
					.findById(taskAssignee.getProjectUser().getIdProjectUser());
			if (existingProjectUser.isPresent()) {
				taskAssignee.setProjectUser(existingProjectUser.get());
			}
			else {
				throw new IllegalArgumentException(
						"ProjectUser with id " + taskAssignee.getProjectUser().getIdProjectUser() + " not found.");
			}
		}
		else {
			throw new IllegalArgumentException("ProjectUser must be provided with a valid id.");
		}

		return taskAssigneesRepository.save(taskAssignee);
	}

	public List<TaskAssignees> getAllTaskAssignees() {
		return taskAssigneesRepository.findAll();
	}

	public Optional<TaskAssignees> getTaskAssigneeById(int id) {
		return taskAssigneesRepository.findById(id);
	}

	public TaskAssignees updateTaskAssignee(int id, TaskAssignees taskAssigneeDetails) {
		return taskAssigneesRepository.findById(id).map(taskAssignee -> {
			// Actualizar ProjectUser si se envía uno nuevo
			if (taskAssigneeDetails.getProjectUser() != null
					&& taskAssigneeDetails.getProjectUser().getIdProjectUser() != 0) {
				Optional<ProjectUser> existingProjectUser = projectUserRepository
						.findById(taskAssigneeDetails.getProjectUser().getIdProjectUser());
				existingProjectUser.ifPresent(taskAssignee::setProjectUser);
			}
			// Actualizar Task si se envía uno nuevo
			if (taskAssigneeDetails.getTask() != null && taskAssigneeDetails.getTask().getId() != 0) {
				Optional<Tasks> existingTask = tasksRepository.findById(taskAssigneeDetails.getTask().getId());
				existingTask.ifPresent(taskAssignee::setTask);
			}
			return taskAssigneesRepository.save(taskAssignee);
		}).orElse(null);
	}

	public boolean deleteTaskAssignee(int id) {
		if (taskAssigneesRepository.existsById(id)) {
			taskAssigneesRepository.deleteById(id);
			return true;
		}
		return false;
	}

	// Obtener todas las asignaciones para un usuario (OracleUser) dado su idUser
	public List<TaskAssignees> getTaskAssigneesByUser(int idUser) {
		return taskAssigneesRepository.findByProjectUserUserIdUser(idUser);
	}

	public List<TaskAssigneeResponseDTO> getTaskAssigneesBySprintId(int sprintId) {
		List<TaskAssignees> taskAssignees = taskAssigneesRepository.findByTaskSprintId(sprintId);

		return taskAssignees.stream().map(this::convertToTaskAssigneeResponseDTO).collect(Collectors.toList());
	}

	private TaskAssigneeResponseDTO convertToTaskAssigneeResponseDTO(TaskAssignees taskAssignee) {
		TaskAssigneeResponseDTO dto = new TaskAssigneeResponseDTO();
		dto.setIdTaskAssignees(taskAssignee.getIdTaskAssignees());

		// Convert ProjectUser
		if (taskAssignee.getProjectUser() != null) {
			ProjectUserDTO projectUserDTO = new ProjectUserDTO(taskAssignee.getProjectUser());
			dto.setProjectUser(projectUserDTO);
		}

		// Convert Task (simplified version)
		if (taskAssignee.getTask() != null) {
			Tasks task = taskAssignee.getTask();
			TaskDTO taskDTO = new TaskDTO(task.getId(), task.getCreationTs(), task.getName(), task.getStatus(),
					task.getDescription(), task.getStoryPoints(), task.getDeadline(), task.getRealHours(),
					task.getEstimatedHours());
			dto.setTask(taskDTO);
		}

		return dto;
	}

	// Obtener asignaciones para un ProjectUser y un Sprint específico
	public List<TaskAssignees> getTaskAssigneesByUserAndSprint(int projectUserId, int sprintId) {
		return taskAssigneesRepository.findByProjectUserIdAndSprintId(projectUserId, sprintId);
	}

	private Double getRealHoursFromTaskAssignees(List<TaskAssignees> taskAssignees) {
		return taskAssignees.stream().map(TaskAssignees::getTask).mapToDouble(Tasks::getRealHours).sum();
	}

	// Nuevos métodos: obtener la cantidad de tareas con status "Done" para un ProjectUser
	// en un Sprint
	// tasks-user-sprint methods (R01)
	public long getCountDoneTasksByUserAndSprint(int projectUserId, int sprintId) {
		return taskAssigneesRepository.countDoneTasksByProjectUserAndSprint(projectUserId, sprintId);
	}

	public List<TaskAssignees> getCompletedTasksByUserAndSprint(int projectUserId, int sprintId) {
		return taskAssigneesRepository.findCompletedTasksByProjectUserAndSprint(projectUserId, sprintId);
	}

	public Double getStaticRealHoursByUserAndSprint(int projectUserId, int sprintId) {
		List<TaskAssignees> taskAssignees = getCompletedTasksByUserAndSprint(projectUserId, sprintId);
		return getRealHoursFromTaskAssignees(taskAssignees);
	}
	

	// tasks-user-daterange methods (R02 y R03)
	public long getCountDoneTasksByUserByDateRange(int projectUserId, LocalDate from, LocalDate to) {
		return taskAssigneesRepository.countDoneTasksByProjectUserAndDateRange(projectUserId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
	}

	public List<TaskAssignees> getCompletedTasksByUserByDateRange(int projectUserId, LocalDate from, LocalDate to) {
		return taskAssigneesRepository.findCompletedTasksByProjectUserAndDateRange(projectUserId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
	}

	public Double getStaticRealHoursByUserByDateRange(int projectUserId, LocalDate from, LocalDate to) {
		List<TaskAssignees> taskAssignees = getCompletedTasksByUserByDateRange(projectUserId, from, to);
		return getRealHoursFromTaskAssignees(taskAssignees);
	}

	// tasks-team-sprint methods (R04)
	public long getCountDoneTasksByTeamAndSprint(int sprintId) {
		return taskAssigneesRepository.countDoneTasksByTeamAndSprint(sprintId);
	}

	public List<TaskAssignees> getCompletedTasksByTeamAndSprint(int sprintId) {
		return taskAssigneesRepository.findCompletedTasksByTeamAndSprint(sprintId);
	}

	public Double getStaticRealHoursByTeamAndSprint(int sprintId) {
		List<TaskAssignees> taskAssignees = getCompletedTasksByTeamAndSprint(sprintId);
		return getRealHoursFromTaskAssignees(taskAssignees);
	}

	// tasks-team-daterange methods (R05 y R06)
	public long getCountDoneTasksByTeamByDateRange(int projectId, LocalDate from, LocalDate to) {
		return taskAssigneesRepository.countDoneTasksByTeamAndDateRange(projectId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
	}

	public List<TaskAssignees> getCompletedTasksByTeamByDateRange(int projectId, LocalDate from, LocalDate to) {
		return taskAssigneesRepository.findCompletedTasksByTeamAndDateRange(projectId, from.atStartOfDay(),
				to.plusDays(1).atStartOfDay());
	}

	public Double getStaticRealHoursByTeamByDateRange(int projectId, LocalDate from, LocalDate to) {
		List<TaskAssignees> taskAssignees = getCompletedTasksByTeamByDateRange(projectId, from, to);
		return getRealHoursFromTaskAssignees(taskAssignees);
	}

}
