package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TaskAssignees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssigneesRepository extends JpaRepository<TaskAssignees, Integer> {
}
