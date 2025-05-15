package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.LoginRequest;
import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.OracleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class OracleUserController {

	@Autowired
	private OracleUserService oracleUserService;

	// Endpoint para registrar un nuevo usuario
	@Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con los datos proporcionados")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
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
	@Operation(summary = "Obtener todos los usuarios", description = "Devuelve una lista de todos los usuarios registrados en el sistema")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
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
	@Operation(summary = "Obtener usuario por ID", description = "Devuelve los datos de un usuario específico según su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Usuario encontrado"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
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
	@Operation(summary = "Login de usuario", description = "Autentica un usuario con nombre y contraseña")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Usuario autenticado correctamente"),
		@ApiResponse(responseCode = "404", description = "Credenciales inválidas o usuario no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
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
	@Operation(summary = "Actualizar usuario parcialmente", description = "Actualiza campos específicos de un usuario como email, status, telegramId, phoneNumber o password")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
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
