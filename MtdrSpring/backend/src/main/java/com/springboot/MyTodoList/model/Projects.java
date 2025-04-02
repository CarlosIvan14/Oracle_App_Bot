package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PROJECTS")
public class Projects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJECT")
    private int id_project;

    @Column(name = "CREATION_TS")
    private LocalDateTime creation_ts;

    @Column(name = "DELETED_TS")
    private LocalDateTime deleted_ts;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME", nullable = false)
    private String name;

    // Relación: Un proyecto puede tener varios usuarios (vía ProjectUser)
    @OneToMany(mappedBy = "projects", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectUser> project_users;

    public Projects() {
    }

    public Projects(int id_project, LocalDateTime creation_ts, LocalDateTime deleted_ts, String description, String name) {
        this.id_project = id_project;
        this.creation_ts = creation_ts;
        this.deleted_ts = deleted_ts;
        this.description = description;
        this.name = name;
    }

    public int getIdProject() {
        return id_project;
    }

    public void setIdProject(int id_project) {
        this.id_project = id_project;
    }

    public LocalDateTime getCreationTs() {
        return creation_ts;
    }

    public void setCreationTs(LocalDateTime creationTs) {
        this.creation_ts = creation_ts;
    }

    public LocalDateTime getDeletedTs() {
        return deleted_ts;
    }

    public void setDeletedTs(LocalDateTime deleted_ts) {
        this.deleted_ts = deleted_ts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProjectUser> getProjectUsers() {
        return project_users;
    }

    public void setProjectUsers(List<ProjectUser> project_users) {
        this.project_users = project_users;
    }

    @Override
    public String toString() {
        return "Project{" +
                "idProject=" + id_project +
                ", creationTs=" + creation_ts +
                ", deletedTs=" + deleted_ts +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}