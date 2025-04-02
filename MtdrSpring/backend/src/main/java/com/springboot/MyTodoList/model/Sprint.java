package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SPRINTS")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SPRINT")
    private int id_sprint;

    @Column(name = "CREATION_TS")
    private LocalDateTime creation_ts;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "ID_PROJECT", nullable = false)
    private Projects project; // Renombrado para claridad

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tasks> tasks;

    public Sprint() {
    }

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
