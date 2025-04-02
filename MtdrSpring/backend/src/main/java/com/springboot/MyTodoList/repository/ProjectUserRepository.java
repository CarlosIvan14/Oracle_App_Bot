package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Integer> {
    // Métodos personalizados, si se requieren
}
