// TasksService.java
package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.dto.AssigneeDTO;
import com.springboot.MyTodoList.dto.ProjectUserDTO;
import com.springboot.MyTodoList.dto.TaskDTO;
import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.repository.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TasksService {

    @Autowired
    private TasksRepository tasksRepository;

    public Tasks addTask(Tasks task) {
        return tasksRepository.save(task);
    }

    public List<Tasks> findAllTasks() {
        return tasksRepository.findAll();
    }

    public Optional<Tasks> getTaskById(int id) {
        return tasksRepository.findById(id);
    }

    public Tasks updateTask(int id, Tasks taskDetails) {
        return tasksRepository.findById(id).map(task -> {
            // Actualización total (PUT)
            task.setCreationTs(taskDetails.getCreationTs());
            task.setName(taskDetails.getName());
            task.setStatus(taskDetails.getStatus());
            task.setDescription(taskDetails.getDescription());
            task.setStoryPoints(taskDetails.getStoryPoints());
            task.setSprint(taskDetails.getSprint());
            task.setDeadline(taskDetails.getDeadline());
            task.setRealHours(taskDetails.getRealHours());
            task.setEstimatedHours(taskDetails.getEstimatedHours());
            return tasksRepository.save(task);
        }).orElse(null);
    }

    // Método para patch: actualiza sólo los campos enviados en el JSON
    public Tasks patchTask(int id, JsonNode taskUpdates) {
        return tasksRepository.findById(id).map(task -> {
            ObjectMapper mapper = new ObjectMapper();
            // Para evitar fallos si se envían propiedades desconocidas
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                // Fusiona (merge) los campos enviados en taskUpdates sobre el objeto task existente
                mapper.readerForUpdating(task).readValue(taskUpdates);
            } catch (IOException e) {
                throw new RuntimeException("Error al actualizar la tarea", e);
            }
            return tasksRepository.save(task);
        }).orElse(null);
    }

    public boolean deleteTask(int id) {
        if (tasksRepository.existsById(id)) {
            tasksRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método para obtener todas las tareas de un Sprint dado su id_sprint
    public List<Tasks> getTasksBySprint(int sprintId) {
        return tasksRepository.findBySprintId(sprintId);
    }

    // Get all unassigned tasks for a sprint
    public List<Tasks> getUnassignedTasksBySprint(int sprintId) {
        return tasksRepository.findUnassignedTasksBySprint(sprintId);
    }

    public List<TaskDTO> getTasksBySprintWithAssignees(int sprintId) {
        // Fetch tasks with their assignees and related user information in a single query
        List<Tasks> tasks = tasksRepository.findTasksWithAssigneesBySprintId(sprintId);
        
        // Convert to DTOs
        return tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }


    private TaskDTO convertToTaskDTO(Tasks task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setCreationTs(task.getCreationTs());
        dto.setName(task.getName());
        dto.setStatus(task.getStatus());
        dto.setDescription(task.getDescription());
        dto.setStoryPoints(task.getStoryPoints());
        dto.setDeadline(task.getDeadline());
        dto.setRealHours(task.getRealHours());
        dto.setEstimatedHours(task.getEstimatedHours());
        
        // Convert assignees if they exist
        if (task.getAssignees() != null) {
            dto.setAssignees(task.getAssignees().stream()
                    .map(assignee -> {
                        AssigneeDTO assigneeDTO = new AssigneeDTO();
                        assigneeDTO.setIdTaskAssignees(assignee.getIdTaskAssignees());
                        
                        // Set ProjectUserDTO if it exists
                        if (assignee.getProjectUser() != null) {
                            ProjectUserDTO projectUserDTO = new ProjectUserDTO();
                            projectUserDTO.setIdProjectUser(assignee.getProjectUser().getIdProjectUser());
                            projectUserDTO.setRoleUser(assignee.getProjectUser().getRoleUser());
                            projectUserDTO.setStatus(assignee.getProjectUser().getStatus());
                            
                            // Set UserDTO if it exists
                            if (assignee.getProjectUser().getUser() != null) {
                                UserDTO userDTO = new UserDTO();
                                userDTO.setIdUser(assignee.getProjectUser().getUser().getIdUser());
                                userDTO.setName(assignee.getProjectUser().getUser().getName());
                                userDTO.setEmail(assignee.getProjectUser().getUser().getEmail());
                                userDTO.setStatus(assignee.getProjectUser().getUser().getStatus());
                                userDTO.setTelegramId(assignee.getProjectUser().getUser().getTelegramId());
                                userDTO.setPhoneNumber(assignee.getProjectUser().getUser().getPhoneNumber());
                                
                                projectUserDTO.setUser(userDTO);
                            }
                            
                            assigneeDTO.setProjectUser(projectUserDTO);
                        }
                        
                        return assigneeDTO;
                    })
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
