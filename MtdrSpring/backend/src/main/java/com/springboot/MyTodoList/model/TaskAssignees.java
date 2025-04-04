package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "TASK_ASSIGNEES")
public class TaskAssignees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TASK_ASSIGNEES")
    private int idTaskAssignees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROJECT_USER", nullable = false)
    private ProjectUser projectUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TASK", nullable = false)
    private Tasks task;

    // Constructor, getters y setters

    public TaskAssignees() {
    }

    public TaskAssignees(int idTaskAssignees, ProjectUser projectUser, Tasks task) {
        this.idTaskAssignees = idTaskAssignees;
        this.projectUser = projectUser;
        this.task = task;
    }

    public int getIdTaskAssignees() {
        return idTaskAssignees;
    }

    public void setIdTaskAssignees(int idTaskAssignees) {
        this.idTaskAssignees = idTaskAssignees;
    }

    public ProjectUser getProjectUser() {
        return projectUser;
    }

    public void setProjectUser(ProjectUser projectUser) {
        this.projectUser = projectUser;
    }

    public Tasks getTask() {
        return task;
    }

    public void setTask(Tasks task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return "TaskAssignees{" +
                "idTaskAssignees=" + idTaskAssignees +
                ", projectUser=" + (projectUser != null ? projectUser.getIdProjectUser() : null) +
                ", task=" + (task != null ? task.getId() : null) +
                '}';
    }
}
