package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Arrays; 
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.springboot.MyTodoList.dto.SimplifiedTaskDTO;
import com.springboot.MyTodoList.model.TaskAssignees; // Import the correct model

@Service
public class TaskServiceBot {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceBot.class);
    
    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "http://localhost:8081/api";

    @Autowired
    public TaskServiceBot(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // Llama al endpoint que obtiene las asignaciones de tareas para un sprint y usuario.
    public List<TaskAssignees> getUserTaskAssignments(int sprintId, int projectUserId) {
        String url = apiBaseUrl + "/task-assignees/user/" + projectUserId + "/sprint/" + sprintId;
        ResponseEntity<TaskAssignees[]> response =
                restTemplate.getForEntity(url, TaskAssignees[].class);

        TaskAssignees[] assignments = response.getBody();
        return (assignments != null) ? Arrays.asList(assignments) : Collections.emptyList();
    }


    public void updateTask(int taskId, Map<String, Object> updates) {
        String url = apiBaseUrl + "/tasks/" + taskId;
    
        // Log the request details
        logger.info("Updating Task ID {} with data: {}", taskId, updates);
    
        // Create the request entity with headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updates, headers);
    
        // Send PATCH request
        ResponseEntity<Void> response = restTemplate.exchange(
            url,
            HttpMethod.PATCH,
            requestEntity,
            Void.class
        );
    
        logger.info("Task update response status: {}", response.getStatusCode());
    }

    public List<SimplifiedTaskDTO> getUnassignedTasksBySprint(int sprintId) {
        String url = apiBaseUrl + "/tasks/unassigned/" + sprintId;
        
        logger.info("Fetching simplified unassigned tasks for sprint {} from: {}", sprintId, url);

        ResponseEntity<List<SimplifiedTaskDTO>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SimplifiedTaskDTO>>() {}
        );

        return response.getBody();
    }

    
}