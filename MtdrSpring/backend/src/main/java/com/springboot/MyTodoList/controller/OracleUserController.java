package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.LoginRequest;
import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.OracleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class OracleUserController {

	@Autowired
	private OracleUserService oracleUserService;

	// Endpoint para registrar un nuevo usuario
	@PostMapping("/register")
	public ResponseEntity<OracleUser> registerUser(@RequestBody OracleUser oracleUser) {
		try {
			OracleUser registeredUser = oracleUserService.registerUser(oracleUser);
			return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Endpoint para obtener todos los usuarios
	@GetMapping
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		try {
			List<UserDTO> users = oracleUserService.getAllUsersAsDTO();
			return new ResponseEntity<>(users, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Endpoint para obtener un usuario por ID
	@GetMapping("/{id}")
	public ResponseEntity<OracleUser> getUserById(@PathVariable int id) {
		try {
			Optional<OracleUser> userOpt = oracleUserService.getUserById(id);
			if (userOpt.isPresent()) {
				return new ResponseEntity<>(userOpt.get(), HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

		// Endpoint para login de usuario
	@PostMapping("/login")
	public ResponseEntity<UserDTO> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
		try {
			Optional<UserDTO> user = oracleUserService.loginUser(
				loginRequest.getName(), 
				loginRequest.getPassword()
			);
			
			return user.map(userDTO -> new ResponseEntity<>(userDTO, HttpStatus.OK))
					.orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Endpoint para actualizar un usuario
	// Se pueden actualizar campos como email, status, telegramId, phoneNumber o password
	@PatchMapping("/{id}")
	public ResponseEntity<UserDTO> updateUser(
			@PathVariable int id, 
			@Valid @RequestBody UserDTO userUpdates) {
		try {
			Optional<UserDTO> updatedUser = oracleUserService.updateUser(id, userUpdates);
			return updatedUser
				.map(userDTO -> new ResponseEntity<>(userDTO, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
