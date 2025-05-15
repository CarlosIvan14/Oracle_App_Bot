package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	@Autowired
	private ProjectService projectService;

	// Create
	@Operation(summary = "Crear nuevo proyecto", description = "Crea un nuevo proyecto con los datos proporcionados")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Proyecto creado exitosamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping
	public ResponseEntity<Projects> createProject(@RequestBody Projects project) {
		Projects createdProject = projectService.addProject(project);
		return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
	}

	// Read All
	@Operation(summary = "Obtener todos los proyectos", description = "Devuelve una lista con todos los proyectos registrados")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyectos obtenidos correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping
	public ResponseEntity<List<Projects>> getAllProjects() {
		List<Projects> projects = projectService.findAllProjects();
		return new ResponseEntity<>(projects, HttpStatus.OK);
	}

	// Read by ID
	@Operation(summary = "Obtener proyecto por ID", description = "Devuelve la información de un proyecto específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyecto encontrado"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping("/{id}")
	public ResponseEntity<Projects> getProjectById(@PathVariable int id) {
		Optional<Projects> project = projectService.getProjectById(id);
		return project.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Update
	@Operation(summary = "Actualizar proyecto", description = "Actualiza completamente los datos de un proyecto existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyecto actualizado correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PutMapping("/{id}")
	public ResponseEntity<Projects> updateProject(@PathVariable int id, @RequestBody Projects projectDetails) {
		Projects updatedProject = projectService.updateProject(id, projectDetails);
		if (updatedProject != null) {
			return new ResponseEntity<>(updatedProject, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Delete
	@Operation(summary = "Eliminar proyecto", description = "Elimina un proyecto por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Proyecto eliminado correctamente"),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProject(@PathVariable int id) {
		boolean deleted = projectService.deleteProject(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}