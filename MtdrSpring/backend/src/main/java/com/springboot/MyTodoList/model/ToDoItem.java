package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/*
    Representation of the TODOITEM table that exists in the autonomous database.
 */
@Entity
@Table(name = "TODOITEM")
public class ToDoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATION_TS")
    private OffsetDateTime creation_ts;

    @Column(name = "DONE")
    private boolean done;

    // Nuevo campo: deadline
    @Column(name = "DEADLINE")
    private LocalDate deadline;

    // Nuevo campo: priority
    @Column(name = "PRIORITY")
    private Integer priority;

    // Relación con ORACLE_USERS
    @ManyToOne
    @JoinColumn(name = "ASSIGNED_USER")
    private OracleUser assignedUser;

    public ToDoItem() {
    }

    public ToDoItem(int ID, String description, OffsetDateTime creation_ts, boolean done) {
        this.ID = ID;
        this.description = description;
        this.creation_ts = creation_ts;
        this.done = done;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreation_ts() {
        return creation_ts;
    }

    public void setCreation_ts(OffsetDateTime creation_ts) {
        this.creation_ts = creation_ts;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public OracleUser getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(OracleUser assignedUser) {
        this.assignedUser = assignedUser;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "ID=" + ID +
                ", description='" + description + '\'' +
                ", creation_ts=" + creation_ts +
                ", done=" + done +
                ", deadline=" + deadline +
                ", priority=" + priority +
                ", assignedUser=" + (assignedUser != null ? assignedUser.getId() : null) +
                '}';
    }
}
