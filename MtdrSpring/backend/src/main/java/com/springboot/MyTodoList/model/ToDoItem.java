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
    private OffsetDateTime creationTs;

    @Column(name = "DONE")
    private boolean done;

    // Nuevo campo: deadline
    @Column(name = "DEADLINE")
    private LocalDate deadline;

    // Nuevo campo: priority
    @Column(name = "PRIORITY")
    private Integer priority;

     // Nuevo campo para el estado de la tarea (por ejemplo, "ASSIGNED", "IN_PROGRESS", "COMPLETED")
     @Column(name = "STATE")
     private String state;

    // Relación con ORACLE_USERS
    @ManyToOne
    @JoinColumn(name = "ASSIGNED_USER") // Nombre de la columna en la tabla TODOITEM
    private OracleUser assignedUser;

    public ToDoItem() {
    }

    public ToDoItem(int ID, String description, OffsetDateTime creationTs, boolean done) {
        this.ID = ID;
        this.description = description;
        this.creationTs = creationTs;
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

    public OffsetDateTime getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(OffsetDateTime creationTs) {
        this.creationTs = creationTs;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "ID=" + ID +
                ", description='" + description + '\'' +
                ", creation_ts=" + creationTs +
                ", done=" + done +
                ", deadline=" + deadline +
                ", priority=" + priority +
                ", state=" + state +
                ", assignedUser=" + (assignedUser != null ? assignedUser.getIdUser() : null) +
                '}';
    }
}
