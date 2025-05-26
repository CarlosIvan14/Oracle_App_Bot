package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.OracleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OracleUserRepository extends JpaRepository<OracleUser, Integer> {

	// Custom query to find a user by name and password
	Optional<OracleUser> findByName(String name);

<<<<<<< HEAD
	@Query("SELECT new com.springboot.MyTodoList.dto.UserDTO(u.idUser, u.name, u.email, u.status, u.telegramId, u.phoneNumber) " +
           "FROM OracleUser u")
    List<UserDTO> findAllUsersAsDTO();

	List<OracleUser> findAll();
=======
>>>>>>> springboot-bot
}