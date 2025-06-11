package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class ProjectsServiceBot {

	private final RestTemplate restTemplate;

	private final String apiBaseUrl = "http://140.84.179.223/api";

	@Autowired
	public ProjectsServiceBot(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public List<Projects> getProjectsByUserId(int userId) {
		String url = apiBaseUrl + "/project-users/user/" + userId + "/projects";
		ResponseEntity<List<Projects>> response = restTemplate.exchange(url, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Projects>>() {
				});
		return response.getBody();
	}

}