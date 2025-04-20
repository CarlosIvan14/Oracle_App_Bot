package com.springboot.MyTodoList.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class TaskAssigneeResponseDTO {
    private int idTaskAssignees;
    private ProjectUserDTO projectUser;
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