package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	@Autowired
	private ProjectService projectService;

	// Create
	@PostMapping
	public ResponseEntity<Projects> createProject(@RequestBody Projects project) {
		Projects createdProject = projectService.addProject(project);
		return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
	}

	// Read All
	@GetMapping
	public ResponseEntity<List<Projects>> getAllProjects() {
		List<Projects> projects = projectService.findAllProjects();
		return new ResponseEntity<>(projects, HttpStatus.OK);
	}

	// Read by ID
	@GetMapping("/{id}")
	public ResponseEntity<Projects> getProjectById(@PathVariable int id) {
		Optional<Projects> project = projectService.getProjectById(id);
		return project.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Update
	@PutMapping("/{id}")
	public ResponseEntity<Projects> updateProject(@PathVariable int id, @RequestBody Projects projectDetails) {
		Projects updatedProject = projectService.updateProject(id, projectDetails);
		if (updatedProject != null) {
			return new ResponseEntity<>(updatedProject, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Delete
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProject(@PathVariable int id) {
		boolean deleted = projectService.deleteProject(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}