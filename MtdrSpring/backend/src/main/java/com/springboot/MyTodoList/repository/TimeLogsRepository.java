package com.springboot.MyTodoList.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.TimeLogs;

@Repository
public interface TimeLogsRepository extends JpaRepository<TimeLogs, Integer> {
    // Fetch timelogs by taskAssignee ID.
    @Query("SELECT tl FROM TimeLogs tl WHERE tl.taskAssignees.id = :taskAssigneeId")
    List<TimeLogs> findByTaskAssigneeID(@Param("taskAssigneeId") int taskAssigneeId);

    // Fetch timelogs by taskAssignee ID Array.
    @Query("SELECT tl FROM TimeLogs tl WHERE tl.taskAssignees.id IN :taskAssigneeIds")
    List<TimeLogs> findByTaskAssigneeIds(@Param("taskAssigneeIds") List<Integer> taskAssigneeIds);

    // Reportes de timeLogs.

    // R07: User-Sprint reports: obtener  los timeLogs
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.projectUser.idProjectUser = :projectUserId " +
       "AND tl.taskAssignees.task.sprint.idSprint = :sprintId " +
       "AND tl.taskAssignees.task.status = 'COMPLETED'")
    List<TimeLogs> findTimeLogsByProjectUserAndSprint(
       @Param("projectUserId") int projectUserId,
       @Param("sprintId") int sprintId);
    
    // R08 y R09: User-From-To reports using completion date between start and end
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.projectUser.idProjectUser = :projectUserId " +
       "AND tl.taskAssignees.task.status = 'COMPLETED' " +
       "AND tl.taskAssignees.task.deadline >= :from " +
       "AND tl.taskAssignees.task.deadline < :to")
    List<TimeLogs> findTimeLogsByProjectUserAndDateRange(
       @Param("projectUserId") int projectUserId,
       @Param("from") LocalDateTime from,
       @Param("to") LocalDateTime to);

    // R10: Team-Sprint reports
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.task.sprint.idSprint = :sprintId " +
       "AND tl.taskAssignees.task.status = 'COMPLETED'")
    List<TimeLogs> findTimeLogsByTeamAndSprint(@Param("sprintId") int sprintId);

    // R11 y R12: Team-Week reports
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.task.sprint.project.idProject = :projectId " +
       "AND tl.taskAssignees.task.status = 'COMPLETED' " +
       "AND tl.taskAssignees.task.deadline >= :from " +
       "AND tl.taskAssignees.task.deadline < :to")
    List<TimeLogs> findTimeLogsByTeamAndDateRange(
       @Param("projectId") int projectId,
       @Param("from") LocalDateTime from,
       @Param("to") LocalDateTime to);
}