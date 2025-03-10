package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.OracleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OracleUserRepository extends JpaRepository<OracleUser, Integer> {
    // Custom query methods can be added here if needed
}