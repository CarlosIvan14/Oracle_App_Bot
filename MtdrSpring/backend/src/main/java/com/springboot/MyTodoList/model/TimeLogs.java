package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TIME_LOGS")
public class TimeLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIME_LOGS")
    private int id_time_logs;

    // Relación ManyToOne con TaskAssignees (tabla TASK_ASSIGNEES)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TASK_ASSIGNEES", nullable = false)
    private TaskAssignees id_task_assignees;

    @Column(name = "START_TS")
    private LocalDateTime start_ts;

    @Column(name = "END_TS")
    private LocalDateTime end_ts;

    public TimeLogs() {
    }

    public TimeLogs(int id_time_logs, TaskAssignees id_task_assignees, LocalDateTime start_ts, LocalDateTime end_ts) {
        this.id_time_logs = id_time_logs;
        this.id_task_assignees = id_task_assignees;
        this.start_ts = start_ts;
        this.end_ts = end_ts;
    }

    public int getIdTimeLogs() {
        return id_time_logs;
    }

    public void setIdTimeLogs(int idTimeLogs) {
        this.id_time_logs = idTimeLogs;
    }

    public TaskAssignees getTaskAssignees() {
        return id_task_assignees;
    }

    public void setTaskAssignees(TaskAssignees id_task_assignees) {
        this.id_task_assignees = id_task_assignees;
    }

    public LocalDateTime getStartTs() {
        return start_ts;
    }

    public void setStartTs(LocalDateTime start_ts) {
        this.start_ts = start_ts;
    }

    public LocalDateTime getEndTs() {
        return end_ts;
    }

    public void setEndTs(LocalDateTime endTs) {
        this.end_ts = end_ts;
    }

    @Override
    public String toString() {
        return "TimeLogs{" +
                "idTimeLogs=" + id_time_logs +
                ", taskAssignees=" + id_task_assignees +
                ", startTs=" + start_ts +
                ", endTs=" + end_ts +
                '}';
    }
}
