package com.springboot.MyTodoList.dto;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.OracleUser;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de un usuario asignado a una tarea")
public class AssigneeDTO {

    @Schema(description = "ID del registro de asignación de tarea", example = "101")
    private int idTaskAssignees;

    @Schema(description = "Usuario asignado al proyecto")
    private ProjectUserDTO projectUser;

	public AssigneeDTO(TaskAssignees taskAssignee) {
		this.idTaskAssignees = taskAssignee.getIdTaskAssignees();
		this.projectUser = new ProjectUserDTO(taskAssignee.getProjectUser());
	}

	public AssigneeDTO() {
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

}