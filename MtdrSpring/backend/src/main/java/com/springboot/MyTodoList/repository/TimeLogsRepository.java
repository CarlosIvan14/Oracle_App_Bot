package com.springboot.MyTodoList.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.TimeLogs;

@Repository
public interface TimeLogsRepository extends JpaRepository<TimeLogs, Integer> {
    // Fetch timelogs by taskAssignee ID.
    @Query("SELECT tl FROM TimeLogs tl WHERE tl.taskAssignees.id = ?1")
    List<TimeLogs> findByTaskAssigneeID(int taskAssigneeId);

    // Fetch timelogs by taskAssignee ID Array.
    @Query("SELECT tl FROM TimeLogs tl WHERE tl.taskAssignees.id IN ?1")
    List<TimeLogs> findByTaskAssigneeIds(List<Integer> taskAssigneeIds);

   //  Reportes de timeLogs.

    // R07: User-Sprint reports: obtener  los timeLogs
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.projectUser.id = ?1 " +
       "AND tl.taskAssignees.task.sprint.id = ?2 " +
       "AND tl.taskAssignees.task.status = 'COMPLETED'")
    List<TimeLogs> findTimeLogsByProjectUserAndSprint(
       int projectUserId,
       int sprintId);
    
    // R08 y R09: User-From-To reports using completion date between start and end
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.projectUser.id = ?1 " +
       "AND tl.taskAssignees.task.status = 'COMPLETED' " +
       "AND tl.taskAssignees.task.deadline >= ?2 " +
       "AND tl.taskAssignees.task.deadline < ?3")
    List<TimeLogs> findTimeLogsByProjectUserAndDateRange(
       int projectUserId,
       LocalDateTime from,
       LocalDateTime to);

    // R10: Team-Sprint reports
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.task.sprint.id = ?1 " +
       "AND tl.taskAssignees.task.status = 'COMPLETED'")
    List<TimeLogs> findTimeLogsByTeamAndSprint(int sprintId);

    // R11 y R12: Team-Week reports
    @Query("SELECT tl FROM TimeLogs tl " +
       "WHERE tl.taskAssignees.task.sprint.project.id = ?1 " +
       "AND tl.taskAssignees.task.status = 'COMPLETED' " +
       "AND tl.taskAssignees.task.deadline >= ?2 " +
       "AND tl.taskAssignees.task.deadline < ?3")
    List<TimeLogs> findTimeLogsByTeamAndDateRange(
       int projectId,
       LocalDateTime from,
       LocalDateTime to);
}