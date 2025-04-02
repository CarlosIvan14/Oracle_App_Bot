package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "TASK_ASSIGNEES")
public class TaskAssignees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TASK_ASSIGNEES")
    private int id_task_assignees;

    // Relación ManyToOne con ProjectUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROJECT_USER", nullable = false)
    private ProjectUser projectUser;

    // Relación ManyToOne con Tasks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TASK", nullable = false)
    private Tasks task;

    public TaskAssignees() {
    }

    public TaskAssignees(int id_task_assignees, ProjectUser projectUser, Tasks task) {
        this.id_task_assignees = id_task_assignees;
        this.projectUser = projectUser;
        this.task = task;
    }

    public int getIdTaskAssignees() {
        return id_task_assignees;
    }

    public void setIdTaskAssignees(int idTaskAssignees) {
        this.id_task_assignees = idTaskAssignees;
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
                "id_task_assignees=" + id_task_assignees +
                ", projectUser=" + projectUser +
                ", task=" + task +
                '}';
    }
}
