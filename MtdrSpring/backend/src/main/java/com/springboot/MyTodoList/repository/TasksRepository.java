package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, Integer> {
    // Devuelve todas las tareas cuyo sprint tenga el id_sprint indicado
    @Query("SELECT t FROM Tasks t WHERE t.sprint.id = :sprintId")
    List<Tasks> findBySprintId(@Param("sprintId") int sprintId);

    @Query("SELECT t FROM Tasks t WHERE t.sprint.id = :sprintId AND t.status = 'UNASSIGNED'")
    List<Tasks> findUnassignedTasksBySprint(@Param("sprintId") int sprintId);

}