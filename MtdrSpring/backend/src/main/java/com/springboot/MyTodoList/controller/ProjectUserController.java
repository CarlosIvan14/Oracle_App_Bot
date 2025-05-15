package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.service.ProjectUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/project-users")
public class ProjectUserController {

	@Autowired
	private ProjectUserService projectUserService;

	// Crear un nuevo ProjectUser
	@Operation(summary = "Asignar usuario a proyecto", description = "Crea una relación entre un usuario y un proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "ProjectUser creado correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping
	public ResponseEntity<ProjectUser> createProjectUser(@RequestBody ProjectUser projectUser) {
		ProjectUser createdProjectUser = projectUserService.addProjectUser(projectUser);
		return new ResponseEntity<>(createdProjectUser, HttpStatus.CREATED);
	}

	// Obtener todos los ProjectUser
	@Operation(summary = "Obtener todas las asignaciones de usuario a proyectos", description = "Devuelve todas las relaciones de usuarios con proyectos")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping
	public ResponseEntity<List<ProjectUser>> getAllProjectUsers() {
		List<ProjectUser> projectUsers = projectUserService.getAllProjectUsers();
		return new ResponseEntity<>(projectUsers, HttpStatus.OK);
	}

	// Obtener un ProjectUser por ID
	@Operation(summary = "Obtener asignaciones por ID", description = "Devuelve la relación específica entre un usuario y un proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Relación encontrada"),
		@ApiResponse(responseCode = "404", description = "Relación no encontrada")
	})
	@GetMapping("/{id}")
	public ResponseEntity<ProjectUser> getProjectUserById(@PathVariable int id) {
		Optional<ProjectUser> projectUserOpt = projectUserService.getProjectUserById(id);
		return projectUserOpt.map(projectUser -> new ResponseEntity<>(projectUser, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar un ProjectUser
	@Operation(summary = "Actualizar parcialmente una relación usuario-proyecto", description = "Permite modificar campos específicos de una relación usuario-proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Relación actualizada correctamente"),
		@ApiResponse(responseCode = "404", description = "Relación no encontrada")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<ProjectUser> patchProjectUser(@PathVariable int id,
			@RequestBody Map<String, Object> updates) {
		ProjectUser patchedProjectUser = projectUserService.patchProjectUser(id, updates);
		if (patchedProjectUser != null) {
			return new ResponseEntity<>(patchedProjectUser, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Actualizar completamente una relación usuario-proyecto", description = "Reemplaza completamente los datos de una relación usuario-proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Relación actualizada correctamente"),
		@ApiResponse(responseCode = "404", description = "Relación no encontrada")
	})
	@PutMapping("/{id}")
	public ResponseEntity<ProjectUser> updateProjectUser(@PathVariable int id,
			@RequestBody ProjectUser projectUserDetails) {
		ProjectUser updatedProjectUser = projectUserService.updateProjectUser(id, projectUserDetails);
		if (updatedProjectUser != null) {
			return new ResponseEntity<>(updatedProjectUser, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar un ProjectUser
	@Operation(summary = "Eliminar una relación usuario-proyecto", description = "Elimina la relación entre un usuario y un proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Relación eliminada correctamente"),
		@ApiResponse(responseCode = "404", description = "Relación no encontrada")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProjectUser(@PathVariable int id) {
		boolean deleted = projectUserService.deleteProjectUser(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Obtener todos los usuarios que pertenecen de un proyecto
	@Operation(summary = "Obtener usuarios por proyecto", description = "Devuelve todos los usuarios asignados a un proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
	})
	@GetMapping("/project/{projectId}/users")
	public ResponseEntity<List<OracleUser>> getUsersByProjectId(@PathVariable int projectId) {
		List<OracleUser> users = projectUserService.getUsersByProjectId(projectId);
		return ResponseEntity.ok(users);
	}

	// Obtener todos los proyectos a los que pertenece un usuario
	@Operation(summary = "Obtener proyectos por usuario", description = "Devuelve todos los proyectos en los que participa un usuario")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista de proyectos obtenida correctamente"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	@GetMapping("/user/{userId}/projects")
	public ResponseEntity<List<Projects>> getProjectsByUserId(@PathVariable int userId) {
		List<Projects> projects = projectUserService.getProjectsByUserId(userId);
		return ResponseEntity.ok(projects);
	}

	@Operation(summary = "Obtener ID de la relación usuario-proyecto", description = "Devuelve el ID de la relación entre un usuario y un proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "ID encontrado correctamente"),
		@ApiResponse(responseCode = "404", description = "Relación no encontrada")
	})
	@GetMapping("/project-id/{projectId}/user-id/{userId}")
	public ResponseEntity<Integer> getProjectUserId(@PathVariable int userId, @PathVariable int projectId) {
		Integer id = projectUserService.getProjectUserIdByUserIdAndProjectId(userId, projectId);
		return id != null ? ResponseEntity.ok(id) : ResponseEntity.notFound().build();
	}

	@Operation(summary = "Obtener rol de usuario en un proyecto", description = "Devuelve el rol de un usuario específico dentro de un proyecto determinado")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Rol obtenido correctamente"),
		@ApiResponse(responseCode = "404", description = "Usuario o proyecto no encontrado")
	})
	@GetMapping("/role-user/project-id/{projectId}/user-id/{userId}")
	public ResponseEntity<String> getRoleUser(@PathVariable int userId, @PathVariable int projectId) {
		String roleUser = projectUserService.getRoleUserByUserIdAndProjectId(userId, projectId);
		return roleUser != null ? ResponseEntity.ok(roleUser) : ResponseEntity.notFound().build();
	}

}
