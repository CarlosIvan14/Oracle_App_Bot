package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "TASK_ASSIGNEES")
public class TaskAssignees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TASK_ASSIGNEES")
    private int id_task_assignees;

    // Relación ManyToOne con ProjectUser (tabla PROJECT_USERS)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROJECT_USER", nullable = false)
    private ProjectUser id_project_user;

    // Relación ManyToOne con Tasks (tabla TASKS)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TASK", nullable = false)
    private Tasks id_task;

    public TaskAssignees() {
    }

    public TaskAssignees(int id_task_assignees, ProjectUser id_project_user, Tasks id_task) {
        this.id_task_assignees = id_task_assignees;
        this.id_project_user = id_project_user;
        this.id_task = id_task;
    }

    public int getIdTaskAssignees() {
        return id_task_assignees;
    }

    public void setIdTaskAssignees(int idTaskAssignees) {
        this.id_task_assignees = idTaskAssignees;
    }

    public ProjectUser getProjectUser() {
        return id_project_user;
    }

    public void setProjectUser(ProjectUser id_project_user) {
        this.id_project_user = id_project_user;
    }

    public Tasks getTask() {
        return id_task;
    }

    public void setTask(Tasks task) {
        this.id_task = id_task;
    }

    @Override
    public String toString() {
        return "TaskAssignees{" +
                "id_task_assignees=" + id_task_assignees +
                ", id_project_user=" + id_project_user +
                ", id_task=" + id_task +
                '}';
    }
}
