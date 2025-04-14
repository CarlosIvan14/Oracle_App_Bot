package com.springboot.MyTodoList.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
       "WHERE ta.projectUser.idProjectUser = :projectUserId " +
       "AND ta.task.status = 'COMPLETED' " +
       "AND ta.task.deadline >= :from " +
       "AND ta.task.deadline < :to")
    long countDoneTasksByProjectUserAndDateRange(
        @Param("projectUserId") int projectUserId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT ta FROM TaskAssignees ta " +
        "WHERE ta.projectUser.idProjectUser = :projectUserId " +
        "AND ta.task.status = 'COMPLETED' " +
        "AND ta.task.deadline >= :from " +
        "AND ta.task.deadline < :to")
    List<TaskAssignees> findCompletedTasksByProjectUserAndDateRange(
        @Param("projectUserId") int projectUserId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
 
    // R04: Team-Sprint reports
    @Query("SELECT COUNT(ta) FROM TaskAssignees ta " +
       "WHERE ta.task.sprint.id = :sprintId " +
       "AND ta.task.status = 'COMPLETED'")
    long countDoneTasksByTeamAndSprint(@Param("sprintId") int sprintId);
    
    @Query("SELECT ta FROM TaskAssignees ta " +
       "WHERE ta.task.sprint.id = :sprintId " +
       "AND ta.task.status = 'COMPLETED'")
    List<TaskAssignees> findCompletedTasksByTeamAndSprint(@Param("sprintId") int sprintId);

    // R05 y R06: Team-Week reports
    @Query("SELECT COUNT(ta) FROM TaskAssignees ta " +
       "WHERE ta.task.status = 'COMPLETED' " +
       "AND ta.task.sprint.project.idProject = :projectId " +
       "AND ta.task.deadline >= :from AND ta.task.deadline < :to")
    long countDoneTasksByTeamAndDateRange(
       @Param("projectId") int projectId,
       @Param("from") LocalDateTime from,
       @Param("to") LocalDateTime to);

       @Query("SELECT ta FROM TaskAssignees ta " +
       "WHERE ta.task.status = 'COMPLETED' " +
       "AND ta.task.sprint.project.idProject = :projectId " +
       "AND ta.task.deadline >= :from AND ta.task.deadline < :to")
    List<TaskAssignees> findCompletedTasksByTeamAndDateRange(
       @Param("projectId") int projectId,
       @Param("from") LocalDateTime from,
       @Param("to") LocalDateTime to);
}
