package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<TaskAssignees> getUserTaskAssignments(int sprintId, int userId) {
        String url = apiBaseUrl + "/task-assignees/user/" + userId + "/sprint/" + sprintId;

        // Log the request URL
        logger.info("Fetching TaskAssignees from: {}", url);

        ResponseEntity<List<TaskAssignees>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<TaskAssignees>>() {}
        );

        logger.info("Response Status: {}", response.getStatusCode());
        return response.getBody();
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
}