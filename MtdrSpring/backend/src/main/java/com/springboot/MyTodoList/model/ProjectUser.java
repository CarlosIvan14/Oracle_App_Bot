package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "PROJECT_USERS")
public class ProjectUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJECT_USER")
    private int id_project_user;

    // Relación ManyToOne con OracleUser (no es bidireccional, por lo que no se requiere anotación de manejo de ciclo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private OracleUser user;

    // Relación ManyToOne con Projects.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROJECT", nullable = false)
    @JsonBackReference  // Este es el lado "hijo" que se omite en la serialización para evitar recursión.
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
        // Evitamos acceder directamente a project para no forzar su carga.
        return "ProjectUser{" +
                "id_project_user=" + id_project_user +
                ", project=" +  project.getIdProject() +
                ", role_user='" + role_user + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
