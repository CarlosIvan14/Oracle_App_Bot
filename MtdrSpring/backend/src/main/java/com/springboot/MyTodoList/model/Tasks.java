package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TASKS")
public class Tasks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TASK")
    private int id_task;

    @Column(name = "CREATION_IS")
    private LocalDateTime creation_ts;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STORY_POINTS")
    private Integer story_points;

    // Renombramos la propiedad a 'sprint' para que coincida con el mappedBy en Sprint
    @ManyToOne
    @JoinColumn(name = "ID_SPRINT", nullable = false)
    private Sprint sprint;

    @Column(name = "DEADLINE")
    private LocalDateTime deadline;

    @Column(name = "REAL_HOURS")
    private Double real_hours;

    @Column(name = "ESTIMATED_HOURS")
    private Double estimated_hours;

    // Getters and Setters
    public int getId() {
        return id_task;
    }

    public void setId(int id_task) {
        this.id_task = id_task;
    }

    public LocalDateTime getCreationTs() {
        return creation_ts;
    }

    public void setCreationTs(LocalDateTime creation_ts) {
        this.creation_ts = creation_ts;
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
        return story_points;
    }

    public void setStoryPoints(Integer story_points) {
        this.story_points = story_points;
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
        return real_hours;
    }

    public void setRealHours(Double real_hours) {
        this.real_hours = real_hours;
    }

    public Double getEstimatedHours() {
        return estimated_hours;
    }

    public void setEstimatedHours(Double estimated_hours) {
        this.estimated_hours = estimated_hours;
    }
}
