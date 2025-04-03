package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SPRINTS")
public class Sprint {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SPRINT")
    @JsonProperty("id_sprint")  // Para que el JSON asigne este valor correctamente
    private int id_sprint;

    @Column(name = "CREATION_TS")
    private LocalDateTime creation_ts;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "ID_PROJECT", nullable = false)
    private Projects project; // Relación con Projects

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Lado "padre" de la relación con Tasks
    private List<Tasks> tasks;

    // Constructor por defecto
    public Sprint() {
    }

    // Constructor completo
    public Sprint(int id_sprint, LocalDateTime creation_ts, String description, String name, Projects project, List<Tasks> tasks) {
        this.id_sprint = id_sprint;
        this.creation_ts = creation_ts;
        this.description = description;
        this.name = name;
        this.project = project;
        this.tasks = tasks;
    }

    // Getters y Setters
    public int getId() {
        return id_sprint;
    }

    public void setId(int id_sprint) {
        this.id_sprint = id_sprint;
    }

    public LocalDateTime getCreationTs() {
        return creation_ts;
    }

    public void setCreationTs(LocalDateTime creation_ts) {
        this.creation_ts = creation_ts;
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

    public Projects getProject() {
        return project;
    }

    public void setProject(Projects project) {
        this.project = project;
    }

    public List<Tasks> getTasks() {
        return tasks;
    }

    public void setTasks(List<Tasks> tasks) {
        this.tasks = tasks;
    }
}
