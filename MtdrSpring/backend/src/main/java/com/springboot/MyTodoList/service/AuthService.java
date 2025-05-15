package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

	private final RestTemplate restTemplate;

	private final String apiBaseUrl = "http://159.54.139.252/api";

	@Autowired
	public AuthService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public OracleUser doLogin(String name, String password) {
		try {
			String url = apiBaseUrl + "/users/login";
			LoginRequest req = new LoginRequest();
			req.setName(name);
			req.setPassword(password);

			ResponseEntity<OracleUser> resp = restTemplate.postForEntity(url, req, OracleUser.class);

			if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
				return resp.getBody();
			}
		}
		catch (Exception e) {
			System.err.println("Error logging in: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
