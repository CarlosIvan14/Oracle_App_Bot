package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SprintsServiceBot {

	private final RestTemplate restTemplate;

	private final String apiBaseUrl = "http://140.84.179.223api";

	@Autowired
	public SprintsServiceBot(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public List<Sprint> getSprintsByProjectId(int projectId) {
		String url = apiBaseUrl + "/sprints/project/" + projectId;
		ResponseEntity<List<Sprint>> response = restTemplate.exchange(url, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Sprint>>() {
				});
		return response.getBody();
	}

}