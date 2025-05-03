package com.springboot.MyTodoList.controller;

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
	public ResponseEntity<List<OracleUser>> getAllUsers() {
		try {
			List<OracleUser> users = oracleUserService.getAllUsers();
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
	public ResponseEntity<OracleUser> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
		try {
			Optional<OracleUser> user = oracleUserService.loginUser(loginRequest.getName(), loginRequest.getPassword());
			if (user.isPresent()) {
				return new ResponseEntity<>(user.get(), HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Endpoint para actualizar un usuario
	// Se pueden actualizar campos como email, status, telegramId, phoneNumber o password
	@PatchMapping("/{id}")
	public ResponseEntity<OracleUser> updateUser(@PathVariable int id, @RequestBody OracleUser userUpdates) {
		try {
			Optional<OracleUser> updatedUser = oracleUserService.updateUser(id, userUpdates);
			if (updatedUser.isPresent()) {
				return new ResponseEntity<>(updatedUser.get(), HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
