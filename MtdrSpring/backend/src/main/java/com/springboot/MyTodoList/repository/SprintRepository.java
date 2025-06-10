package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {

	// Utilizamos "_" para indicar que se accede a la propiedad idProject del objeto
	// project.
	List<Sprint> findByProject_IdProject(int projectId);

	List<Sprint> findByProject_IdProjectOrderByCreationTsAsc(int projectId);

	List<Sprint> findAllByOrderByCreationTsDesc();

}
