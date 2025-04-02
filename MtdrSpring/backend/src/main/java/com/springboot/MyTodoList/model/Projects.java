package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "PROJECTS")
public class Projects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJECT")
    private int idProject;

    @Column(name = "CREATION_TS")
    private LocalDateTime creation_ts;

    @Column(name = "DELETED_TS")
    private LocalDateTime deleted_ts;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME", nullable = false)
    private String name;

    // Un proyecto tiene muchos ProjectUser.
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Este es el lado "padre" de la relación.
    private List<ProjectUser> projectUsers;

    public Projects() {
    }

    public Projects(int idProject, LocalDateTime creation_ts, LocalDateTime deleted_ts, String description, String name) {
        this.idProject = idProject;
        this.creation_ts = creation_ts;
        this.deleted_ts = deleted_ts;
        this.description = description;
        this.name = name;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public LocalDateTime getCreationTs() {
        return creation_ts;
    }

    public void setCreationTs(LocalDateTime creation_ts) {
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
        return projectUsers;
    }

    public void setProjectUsers(List<ProjectUser> projectUsers) {
        this.projectUsers = projectUsers;
    }

    @Override
    public String toString() {
        return "Projects{" +
                "idProject=" + idProject +
                ", creation_ts=" + creation_ts +
                ", deleted_ts=" + deleted_ts +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
