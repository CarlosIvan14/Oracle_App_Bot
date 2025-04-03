package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.Projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Integer> {
    // Fetch users by project ID (directly from ProjectUsers table)
    @Query("SELECT pu.user FROM ProjectUser pu WHERE pu.project.id = :projectId")
    List<OracleUser> findUsersByProjectId(int projectId);

    @Query("SELECT pu.project FROM ProjectUser pu WHERE pu.user.id = :userId")
    List<Projects> findProjectsByUserId(int userId);
}
