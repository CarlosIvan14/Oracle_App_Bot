package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

@Service
public class TaskCreationServiceBot {
    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "http://localhost:8081/api";
    private final UserRoleServiceBot userRoleService;

    @Autowired
    public TaskCreationServiceBot(RestTemplateBuilder restTemplateBuilder, 
                                UserRoleServiceBot userRoleService) {
        this.restTemplate = restTemplateBuilder.build();
        this.userRoleService = userRoleService;
    }

    // Get project user ID for automatic assignment
    public Integer getProjectUserId(int projectId, int userId) {
        String url = apiBaseUrl + "/project-users/project-id/" + projectId + "/user-id/" + userId;
        ResponseEntity<ProjectUser> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            ProjectUser.class
        );
        return response.getBody() != null ? response.getBody().getIdProjectUser() : null;
    }

    // Create a new task
    public Tasks createTask(Tasks task) {
        String url = apiBaseUrl + "/tasks";
        HttpEntity<Tasks> request = new HttpEntity<>(task);
        ResponseEntity<Tasks> response = restTemplate.postForEntity(url, request, Tasks.class);
        return response.getBody();
    }

    public TaskAssignees assignTask(int taskId, int projectUserId) {
        String url = apiBaseUrl + "/task-assignees";
        
        // Create Task reference with just the ID
        Tasks task = new Tasks();
        task.setId(taskId);
        
        // Create ProjectUser reference with just the ID
        ProjectUser projectUser = new ProjectUser();
        projectUser.setIdProjectUser(projectUserId);
        
        // Create the assignment
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

    // Combined method for developer workflow
    public Tasks createDeveloperTask(Tasks task, int projectId, int userId) {
        // 1. Get project user ID
        Integer projectUserId = getProjectUserId(projectId, userId);
        if (projectUserId == null) {
            throw new RuntimeException("User not found in project");
        }
    
        // 2. Create the task (status should be "ASSIGNED" for developer)
        task.setStatus("ASSIGNED");
        Tasks createdTask = createTask(task);
        
        // 3. Auto-assign to developer
        assignTask(createdTask.getId(), projectUserId);
        
        return createdTask;
    }
}