package com.springboot.MyTodoList.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Skills;

@Repository
public interface SkillsRepository extends JpaRepository<Skills, Integer> {

	// Fetch skills by oracleUser ID.
	@Query("SELECT s FROM Skills s WHERE s.oracleUser.id = :oracleUserId")
	List<Skills> findByOracleUserID(@Param("oracleUserId") int oracleUserId);

}