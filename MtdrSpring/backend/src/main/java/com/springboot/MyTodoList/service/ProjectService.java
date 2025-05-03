package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

	@Autowired
	private ProjectsRepository projectsRepository;

	// Create
	public Projects addProject(Projects project) {
		return projectsRepository.save(project);
	}

	// Read All
	public List<Projects> findAllProjects() {
		return projectsRepository.findAll();
	}

	// Read by ID
	public Optional<Projects> getProjectById(int id) {
		return projectsRepository.findById(id);
	}

	// Update
	public Projects updateProject(int id, Projects projectDetails) {
		Optional<Projects> projectOptional = projectsRepository.findById(id);
		if (projectOptional.isPresent()) {
			Projects project = projectOptional.get();
			project.setName(projectDetails.getName());
			project.setDescription(projectDetails.getDescription());
			project.setCreationTs(projectDetails.getCreationTs());
			project.setDeletedTs(projectDetails.getDeletedTs());
			return projectsRepository.save(project);
		}
		return null; // or throw an exception
	}

	// Delete
	public boolean deleteProject(int id) {
		if (projectsRepository.existsById(id)) {
			projectsRepository.deleteById(id);
			return true;
		}
		return false;
	}

}