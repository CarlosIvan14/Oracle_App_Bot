package com.springboot.MyTodoList.dto;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.OracleUser;

public class AssigneeDTO {
    private int idTaskAssignees;
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