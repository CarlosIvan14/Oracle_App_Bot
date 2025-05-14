package com.springboot.MyTodoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserRoleServiceBot {

	private final RestTemplate rest;

	private final String apiBase = "http://140.84.170.68/api";

	@Autowired
	public UserRoleServiceBot(RestTemplateBuilder builder) {
		this.rest = builder.build();
	}

	/**
	 * Devuelve "manager" | "developer". Si el backend responde 404 (usuario no asociado
	 * al proyecto) asumimos "developer".
	 */
	public String getUserRoleInProject(int projectId, int userId) {

		String url = String.format("%s/project-users/role-user/project-id/%d/user-id/%d", apiBase, projectId, userId);

		try {
			ResponseEntity<String> r = rest.exchange(url, HttpMethod.GET, null, String.class);
			return r.getBody();
		}
		catch (HttpClientErrorException.NotFound nf) {
			// No hay ProjectUser → no es manager
			return "developer";
		}
		catch (Exception e) {
			// Cualquier otra cosa se considera developer y se registra en log
			System.err.println("Error consultando rol: " + e.getMessage());
			return "developer";
		}
	}

	public boolean isManagerInProject(int projectId, int userId) {
		return "manager".equalsIgnoreCase(getUserRoleInProject(projectId, userId));
	}

}
