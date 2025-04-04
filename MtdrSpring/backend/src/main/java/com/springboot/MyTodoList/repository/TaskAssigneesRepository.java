package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TaskAssignees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAssigneesRepository extends JpaRepository<TaskAssignees, Integer> {

    // Método derivado previamente usado:
    List<TaskAssignees> findByProjectUserUserIdUser(int idUser);

    // Método con @Query para obtener asignaciones por ProjectUser y Sprint
    @Query("select ta from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2")
    List<TaskAssignees> findByProjectUserIdAndSprintId(int projectUserId, int sprintId);

    // Nuevo método: obtener el count de tareas con status "COMPLETED"
    @Query("select count(ta) from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2 and ta.task.status = 'COMPLETED'")
    long countDoneTasksByProjectUserAndSprint(int projectUserId, int sprintId);
    @Query("select ta from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2 and ta.task.status = 'COMPLETED'")
    List<TaskAssignees> findCompletedTasksByProjectUserAndSprint(int projectUserId, int sprintId);

}
