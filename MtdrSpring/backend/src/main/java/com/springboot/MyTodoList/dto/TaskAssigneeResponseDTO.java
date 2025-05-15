package com.springboot.MyTodoList.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Schema(description = "Asignación de una tarea a un usuario dentro de un sprint")
public class TaskAssigneeResponseDTO {

	@Schema(description = "ID de la asignación de tarea", example = "123")
    private int idTaskAssignees;

    @Schema(description = "Usuario asignado al proyecto")
    private ProjectUserDTO projectUser;

    @Schema(description = "Tarea asignada al usuario")
    private TaskDTO task; // Simplified version of TaskDTO

	// Constructors, Getters and Setters
	public TaskAssigneeResponseDTO() {
	}

	public TaskAssigneeResponseDTO(int idTaskAssignees, ProjectUserDTO projectUser, TaskDTO task) {
		this.idTaskAssignees = idTaskAssignees;
		this.projectUser = projectUser;
		this.task = task;
	}

	// Getters and Setters
	public int getIdTaskAssignees() {
		return idTaskAssignees;
	}

	public void setIdTaskAssignees(int idTaskAssignees) {
		this.idTaskAssignees = idTaskAssignees;
	}

	public ProjectUserDTO getProjectUser() {
		return projectUser;
	}

	public void setProjectUser(ProjectUserDTO projectUser) {
		this.projectUser = projectUser;
	}

	public TaskDTO getTask() {
		return task;
	}

	public void setTask(TaskDTO task) {
		this.task = task;
	}

}