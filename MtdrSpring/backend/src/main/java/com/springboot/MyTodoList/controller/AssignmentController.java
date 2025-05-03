package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.OpenAIService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {

	private final OpenAIService openAIService;

	public AssignmentController(OpenAIService openAIService) {
		this.openAIService = openAIService;
	}

	/**
	 * Body esperado: { "projectId" : 41, "name" : "Implementar pasarela de pagos",
	 * "description" : "Crear micro‑servicio ..." }
	 */
	@PostMapping("/by-ai")
	public ResponseEntity<List<OracleUser>> assignByAi(@RequestBody Map<String, Object> payload) {

		// 1. validar
		if (!payload.containsKey("projectId") || !payload.containsKey("name") || !payload.containsKey("description"))
			return ResponseEntity.badRequest().build();

		int projectId = Integer.parseInt(payload.get("projectId").toString());
		String taskName = payload.get("name").toString();
		String description = payload.get("description").toString();

		try {
			List<OracleUser> users = openAIService.rankUsersForTask(projectId, taskName, description);
			return ResponseEntity.ok(users);
		}
		catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
