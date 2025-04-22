package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class TaskCreationServiceBot {
    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "http://localhost:8081/api";
    private final UserRoleServiceBot userRoleService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TaskCreationServiceBot(RestTemplateBuilder restTemplateBuilder, 
                                  UserRoleServiceBot userRoleService) {
        this.restTemplate = restTemplateBuilder.build();
        this.userRoleService = userRoleService;
    }
    // Método para obtener la referencia del Sprint existente
    public Sprint getSprintReference(int sprintId) {
        return entityManager.getReference(Sprint.class, sprintId);
    }
    // Get project user ID for automatic assignment
    public Integer getProjectUserId(int projectId, int userId) {
        String url = apiBaseUrl + "/project-users/project-id/" + projectId + "/user-id/" + userId;
        ResponseEntity<Integer> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            Integer.class
        );
        return response.getBody();
    }

    // Create a new task
    public Tasks createTask(Tasks task) {
        // LOGUEA el objeto que enviarás:
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("JSON que se enviará = " + mapper.writeValueAsString(task));
        } catch (Exception e) {}
        
        String url = apiBaseUrl + "/tasks";
        HttpEntity<Tasks> request = new HttpEntity<>(task);
        ResponseEntity<Tasks> response = restTemplate.postForEntity(url, request, Tasks.class);
        return response.getBody();
    }


    public TaskAssignees assignTask(int taskId, int projectUserId) {
        String url = apiBaseUrl + "/task-assignees";
        
        Tasks task = new Tasks();
        task.setId(taskId);
        
        ProjectUser projectUser = new ProjectUser();
        projectUser.setIdProjectUser(projectUserId);
        
        TaskAssignees assignment = new TaskAssignees();
        assignment.setTask(task);
        assignment.setProjectUser(projectUser);
        
        HttpEntity<TaskAssignees> request = new HttpEntity<>(assignment);
        ResponseEntity<TaskAssignees> response = restTemplate.postForEntity(
            url, 
            request, 
            TaskAssignees.class
        );
        return response.getBody();
    }

    // Método combinado para flujo de desarrollador
    // Método combinado para flujo de desarrollador
    public Tasks createDeveloperTask(Tasks task, int projectId, int userId) {
        Integer projectUserId = getProjectUserId(projectId, userId);
        if (projectUserId == null) {
            throw new RuntimeException("User not found in project");
        }

        // En vez de usar getSprintReference(), creamos un objeto Sprint simple
        if (task.getSprint() != null) {
            Sprint sprint = new Sprint();
            sprint.setId(task.getSprint().getId());
            task.setSprint(sprint);
        }

        task.setStatus("ASSIGNED");
        Tasks createdTask = createTask(task);
        assignTask(createdTask.getId(), projectUserId);
        return createdTask;
    }

}