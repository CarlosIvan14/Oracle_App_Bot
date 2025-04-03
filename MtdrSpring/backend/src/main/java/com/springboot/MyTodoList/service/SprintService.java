package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;
    
    @Autowired
    private ProjectsRepository projectsRepository; // Repositorio para manejar Projects

    public Sprint addSprint(Sprint sprint) {
        // Verifica si se ha enviado un project con id
        if (sprint.getProject() != null && sprint.getProject().getIdProject() != 0) {
            int projectId = sprint.getProject().getIdProject();
            // Carga el project existente desde la base de datos
            Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with id " + projectId + " not found."));
            sprint.setProject(project);
        }
        return sprintRepository.save(sprint);
    }

    public List<Sprint> findAllSprints() {
        return sprintRepository.findAll();
    }

    public Optional<Sprint> getSprintById(int id) {
        return sprintRepository.findById(id);
    }

    public Sprint updateSprint(int id, Sprint sprintDetails) {
        return sprintRepository.findById(id).map(sprint -> {
            sprint.setCreationTs(sprintDetails.getCreationTs());
            sprint.setDescription(sprintDetails.getDescription());
            sprint.setName(sprintDetails.getName());
            // Actualiza el project de forma similar: si se envía el id, carga la entidad gestionada
            if (sprintDetails.getProject() != null && sprintDetails.getProject().getIdProject() != 0) {
                int projectId = sprintDetails.getProject().getIdProject();
                Projects project = projectsRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project with id " + projectId + " not found."));
                sprint.setProject(project);
            }
            sprint.setTasks(sprintDetails.getTasks());
            return sprintRepository.save(sprint);
        }).orElse(null);
    }

    public Sprint patchSprint(int id, Map<String, Object> updates) {
        return sprintRepository.findById(id).map(sprint -> {
            if (updates.containsKey("creation_ts")) {
                // Se espera que el valor sea una cadena en formato ISO_LOCAL_DATE_TIME
                String creationTsStr = updates.get("creation_ts").toString();
                sprint.setCreationTs(LocalDateTime.parse(creationTsStr));
            }
            if (updates.containsKey("description")) {
                sprint.setDescription(updates.get("description").toString());
            }
            if (updates.containsKey("name")) {
                sprint.setName(updates.get("name").toString());
            }
            // Para actualizar el project se puede recibir el id, por ejemplo "projectId"
            if (updates.containsKey("projectId")) {
                int projectId = Integer.parseInt(updates.get("projectId").toString());
                Projects project = projectsRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project with id " + projectId + " not found."));
                sprint.setProject(project);
            }
            // Se pueden agregar más campos si es necesario

            return sprintRepository.save(sprint);
        }).orElse(null);
    }

    public boolean deleteSprint(int id) {
        if (sprintRepository.existsById(id)) {
            sprintRepository.deleteById(id);
            return true;
        }
        return false;
    }
    // Nuevo método para obtener sprints por id de proyecto
    public List<Sprint> findSprintsByProjectId(int projectId) {
        return sprintRepository.findByProject_IdProject(projectId);
    }    
}
