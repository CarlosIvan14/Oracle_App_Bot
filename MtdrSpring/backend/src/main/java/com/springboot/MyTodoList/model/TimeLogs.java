package com.springboot.MyTodoList.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TIME_LOGS")
public class TimeLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIME_LOGS")
    private int idTimeLogs;

    // Relación ManyToOne con TaskAssignees
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TASK_ASSIGNEES", nullable = false)
    private TaskAssignees taskAssignees;

    @Column(name = "START_TS")
    private LocalDateTime startTs;

    @Column(name = "END_TS")
    private LocalDateTime endTs;

    public TimeLogs() {}

    public TimeLogs(TaskAssignees taskAssignees) {
        this.taskAssignees = taskAssignees;
        this.startTs = LocalDateTime.now();
    }

    public TimeLogs(TaskAssignees taskAssignees, LocalDateTime startTs, LocalDateTime endTs) {
        this.taskAssignees = taskAssignees;
        this.startTs = startTs;
        this.endTs = endTs;
    }

    public int getIdTimeLogs() {
        return idTimeLogs;
    }

    public void setIdTimeLogs(int idTimeLogs) {
        this.idTimeLogs = idTimeLogs;
    }

    public TaskAssignees getTaskAssignees() {
        return taskAssignees;
    }

    public void setTaskAssignees(TaskAssignees taskAssignees) {
        this.taskAssignees = taskAssignees;
    }

    public LocalDateTime getStartTs() {
        return startTs;
    }

    public void setStartTs(LocalDateTime startTs) {
        this.startTs = startTs;
    }

    public LocalDateTime getEndTs() {
        return endTs;
    }

    public void setEndTs(LocalDateTime endTs) {
        this.endTs = endTs;
    }

    @Override
    public String toString() {
        return "TimeLogs{" +
                "idTimeLogs=" + idTimeLogs +
                ", taskAssignees=" + taskAssignees.getIdTaskAssignees() +
                ", startTs=" + startTs +
                ", endTs=" + endTs +
                '}';
    }
}
