package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@EnableTransactionManagement
public interface ProjectRepository extends JpaRepository<Projects, Integer> {
    // Basic CRUD operations are provided by JpaRepository
}