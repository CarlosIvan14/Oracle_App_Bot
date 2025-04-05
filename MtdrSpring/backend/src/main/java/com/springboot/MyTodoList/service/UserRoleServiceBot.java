package com.springboot.MyTodoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserRoleServiceBot {
    private final RestTemplate restTemplate;
    private final String apiBaseUrl = "http://159.54.153.189/api";

    @Autowired
    public UserRoleServiceBot(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getUserRoleInProject(int projectId, int userId) {
        String url = String.format("%s/project-users/role-user/project-id/%d/user-id/%d", 
                                 apiBaseUrl, projectId, userId);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            String.class
        );
        
        return response.getBody(); // Returns "manager" or "developer"
    }

    public boolean isManagerInProject(int projectId, int userId) {
        String role = getUserRoleInProject(projectId, userId);
        return "manager".equalsIgnoreCase(role);
    }
}
