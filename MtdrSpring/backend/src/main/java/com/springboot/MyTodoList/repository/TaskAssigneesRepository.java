package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TaskAssignees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAssigneesRepository extends JpaRepository<TaskAssignees, Integer> {
    
    // Método para obtener asignaciones por id del ProjectUser y del Sprint, usando una consulta JPQL
    @Query("select ta from TaskAssignees ta where ta.projectUser.idProjectUser = ?1 and ta.task.sprint.id = ?2")
    List<TaskAssignees> findByProjectUserIdAndSprintId(int projectUserId, int sprintId);

    List<TaskAssignees> findByProjectUserUserIdUser(int idUser);
}
