package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.OracleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OracleUserRepository extends JpaRepository<OracleUser, Integer> {
    // Custom query to find a user by name and password
    Optional<OracleUser> findByName(String name);
}