package com.springboot.MyTodoList.dto;

import com.springboot.MyTodoList.model.ProjectUser;

public class ProjectUserDTO {
    private int idProjectUser;
    private UserDTO user;
    private String roleUser;
    private String status;

    public ProjectUserDTO(ProjectUser projectUser) {
        this.idProjectUser = projectUser.getIdProjectUser();
        this.user = new UserDTO(projectUser.getUser());
        this.roleUser = projectUser.getRoleUser();
        this.status = projectUser.getStatus();
    }

    public ProjectUserDTO() {
    }

    // Getters and Setters
    public int getIdProjectUser() {
        return idProjectUser;
    }

    public void setIdProjectUser(int idProjectUser) {
        this.idProjectUser = idProjectUser;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getRoleUser() {
        return roleUser;
    }

    public void setRoleUser(String roleUser) {
        this.roleUser = roleUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}