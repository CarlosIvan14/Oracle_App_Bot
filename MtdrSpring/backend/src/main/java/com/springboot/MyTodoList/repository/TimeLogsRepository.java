package com.springboot.MyTodoList.repository;

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
}