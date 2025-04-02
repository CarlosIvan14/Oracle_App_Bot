package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectsRepository extends JpaRepository<Projects, Integer> {
    // Métodos personalizados, si se requieren
}
