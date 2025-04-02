package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    // Create
    public Projects addProject(Projects project) {
        return projectRepository.save(project);
    }

    // Read All
    public List<Projects> findAllProjects() {
        return projectRepository.findAll();
    }

    // Read by ID
    public Optional<Projects> getProjectById(int id) {
        return projectRepository.findById(id);
    }

    // Update
    public Projects updateProject(int id, Projects projectDetails) {
        Optional<Projects> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Projects project = projectOptional.get();
            project.setName(projectDetails.getName());
            project.setDescription(projectDetails.getDescription());
            project.setCreationTs(projectDetails.getCreationTs());
            project.setDeletedTs(projectDetails.getDeletedTs());
            return projectRepository.save(project);
        }
        return null; // or throw an exception
    }

    // Delete
    public boolean deleteProject(int id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }
}