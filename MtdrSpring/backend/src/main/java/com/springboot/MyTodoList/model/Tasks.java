package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;

import javax.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "TASKS")
public class Tasks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TASK")
    private int idTask;

    @ManyToOne
    @JoinColumn(name = "ID_SPRINT", nullable = false)
    private Sprint sprint;


    @Column(name = "CREATION_IS")
    private LocalDateTime creationTs;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STORY_POINTS")
    private Integer storyPoints;

    @Column(name = "DEADLINE")
    private LocalDateTime deadline;

    @Column(name = "REAL_HOURS")
    private Double realHours;

    @Column(name = "ESTIMATED_HOURS")
    private Double estimatedHours;

    // Constructor por defecto
    public Tasks() {
    }

    // Constructor completo (excepto id_task)
    public Tasks(LocalDateTime creationTs, String name, String status, String description, Integer storyPoints,
                 Sprint sprint, LocalDateTime deadline, Double realHours, Double estimatedHours) {
        this.creationTs = creationTs;
        this.name = name;
        this.status = status;
        this.description = description;
        this.storyPoints = storyPoints;
        this.sprint = sprint;
        this.deadline = deadline;
        this.realHours = realHours;
        this.estimatedHours = estimatedHours;
    }

    // Getters y Setters

    public int getId() {
        return idTask;
    }

    public void setId(int idTask) {
        this.idTask = idTask;
    }

    public LocalDateTime getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(LocalDateTime creationTs) {
        this.creationTs = creationTs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Double getRealHours() {
        return realHours;
    }

    public void setRealHours(Double realHours) {
        this.realHours = realHours;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
}