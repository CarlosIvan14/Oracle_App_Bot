package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "PROJECT_USERS")
public class ProjectUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJECT_USER")
    private int id_project_user;

    // Relación ManyToOne con OracleUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private OracleUser user;

    // Renombramos de id_project a project para que coincida con el mappedBy en Projects
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROJECT", nullable = false)
    private Projects project;

    @Column(name = "ROLE_USER")
    private String role_user;

    @Column(name = "STATUS")
    private String status;

    public ProjectUser() {
    }

    public ProjectUser(int id_project_user, OracleUser user, Projects project, String role_user, String status) {
        this.id_project_user = id_project_user;
        this.user = user;
        this.project = project;
        this.role_user = role_user;
        this.status = status;
    }

    public int getIdProjectUser() {
        return id_project_user;
    }

    public void setIdProjectUser(int id_project_user) {
        this.id_project_user = id_project_user;
    }

    public OracleUser getUser() {
        return user;
    }

    public void setUser(OracleUser user) {
        this.user = user;
    }

    public Projects getProject() {
        return project;
    }

    public void setProject(Projects project) {
        this.project = project;
    }

    public String getRoleUser() {
        return role_user;
    }

    public void setRoleUser(String roleUser) {
        this.role_user = roleUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ProjectUser{" +
                "id_project_user=" + id_project_user +
                ", project=" + project +
                ", role_user='" + role_user + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
