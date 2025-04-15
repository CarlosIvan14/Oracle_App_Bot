package com.springboot.MyTodoList.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.TaskAssignees;

@Repository
public interface TaskAssigneesRepository extends JpaRepository<TaskAssignees, Integer> {

    // Método derivado previamente usado:
    List<TaskAssignees> findByProjectUserUserIdUser(int idUser);

    // Método con @Query para obtener asignaciones por ProjectUser y Sprint
    @Query("select ta from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2")
    List<TaskAssignees> findByProjectUserIdAndSprintId(int projectUserId, int sprintId);

    // Reportes de tasks.

    // R01: User-Sprint reports: obtener el count de tareas con status "COMPLETED"
    // FLOW: projectUserId+sprintId ->  -> count(done tasks)
    @Query("select count(ta) from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2 and ta.task.status = 'COMPLETED'")
    long countDoneTasksByProjectUserAndSprint(int projectUserId, int sprintId);

    @Query("select ta from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2 and ta.task.status = 'COMPLETED'")
    List<TaskAssignees> findCompletedTasksByProjectUserAndSprint(int projectUserId, int sprintId);
    
    // R02 y R03: User-From-To reports using completion date between start and end
    @Query("SELECT COUNT(ta) FROM TaskAssignees ta " +
       "WHERE ta.projectUser.idProjectUser = ?1 " +
       "AND ta.task.status = 'COMPLETED' " +
       "AND ta.task.deadline >= ?2 " +
       "AND ta.task.deadline < ?3")
    long countDoneTasksByProjectUserAndDateRange(
        int projectUserId,
        LocalDateTime from,
        LocalDateTime to);

    @Query("SELECT ta FROM TaskAssignees ta " +
        "WHERE ta.projectUser.idProjectUser = ?1 " +
        "AND ta.task.status = 'COMPLETED' " +
        "AND ta.task.deadline >= ?2 " +
        "AND ta.task.deadline < ?3")
    List<TaskAssignees> findCompletedTasksByProjectUserAndDateRange(
         int projectUserId,
         LocalDateTime from,
         LocalDateTime to);
 
    // R04: Team-Sprint reports
    @Query("SELECT COUNT(ta) FROM TaskAssignees ta " +
       "WHERE ta.task.sprint.id = ?1 " +
       "AND ta.task.status = 'COMPLETED'")
    long countDoneTasksByTeamAndSprint(int sprintId);
    
    @Query("SELECT ta FROM TaskAssignees ta " +
       "WHERE ta.task.sprint.id = ?1 " +
       "AND ta.task.status = 'COMPLETED'")
    List<TaskAssignees> findCompletedTasksByTeamAndSprint(int sprintId);

    // R05 y R06: Team-Week reports
    @Query("SELECT COUNT(ta) FROM TaskAssignees ta " +
       "WHERE ta.task.status = 'COMPLETED' " +
       "AND ta.task.sprint.project.idProject = ?1 " +
       "AND ta.task.deadline >= ?2 AND ta.task.deadline < ?3")
    long countDoneTasksByTeamAndDateRange(
       int projectId,
       LocalDateTime from,
       LocalDateTime to);

    @Query("SELECT ta FROM TaskAssignees ta " +
       "WHERE ta.task.status = 'COMPLETED' " +
       "AND ta.task.sprint.project.idProject = ?1 " +
       "AND ta.task.deadline >= ?2 AND ta.task.deadline < ?3")
    List<TaskAssignees> findCompletedTasksByTeamAndDateRange(
       int projectId,
       LocalDateTime from,
       LocalDateTime to);
}
