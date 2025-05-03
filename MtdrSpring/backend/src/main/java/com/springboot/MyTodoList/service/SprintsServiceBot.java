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

	<<<<<<<HEAD
	private final RestTemplate restTemplate;

	private final String apiBaseUrl = "http://140.84.170.68/api";

	=======>>>>>>>57a64cb (backend format checking w springjavaformat && testNG)

	private final RestTemplate restTemplate;

	private final String apiBaseUrl = "http://localhost:8081/api";

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