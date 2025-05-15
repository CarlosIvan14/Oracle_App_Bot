package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.OpenAIService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {

	private final OpenAIService openAIService;

	public AssignmentController(OpenAIService openAIService) {
		this.openAIService = openAIService;
	}

	@Operation(
		summary = "Asignar usuarios automaticamente con IA",
		description = "Recibe ID del proyecto, nombre y descripcion de la tarea para recomendar a los usuarios"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Usuarios recomendados exitosamente",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = OracleUser.class)))),
		@ApiResponse(responseCode = "400", description = "Parametros invalidos"),
		@ApiResponse(responseCode = "500", description = "Error del servidor")
	})
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
