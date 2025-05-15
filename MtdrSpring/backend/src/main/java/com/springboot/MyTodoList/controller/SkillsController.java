package com.springboot.MyTodoList.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.MyTodoList.model.Skills;
import com.springboot.MyTodoList.service.SkillsService;

@RestController
@RequestMapping("/api/skills")
public class SkillsController {

	@Autowired
	private SkillsService skillsService;

	// Crear Skill
	@Operation(summary = "Crear nueva habilidad", description = "Crea una nueva habilidad para el usuario")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Habilidad creada exitosamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@PostMapping
	public ResponseEntity<Skills> createSkill(@RequestBody Skills skill) {
		Skills createdSkill = skillsService.addSkills(skill);
		return new ResponseEntity<>(createdSkill, HttpStatus.CREATED);
	}

	// Obtener todos las skills
	@Operation(summary = "Obtener todas las habilidades", description = "Devuelve la lista completa de habilidades registradas")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
		@ApiResponse(responseCode = "500", description = "Error interno del servidor")
	})
	@GetMapping
	public ResponseEntity<List<Skills>> getAllSkills() {
		List<Skills> skills = skillsService.findAllSkills();
		return new ResponseEntity<>(skills, HttpStatus.OK);
	}

	// Obtener Skill por ID
	@Operation(summary = "Obtener habilidad por ID", description = "Devuelve la información de una habilidad específica")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Habilidad encontrada"),
		@ApiResponse(responseCode = "404", description = "Habilidad no encontrada")
	})
	@GetMapping("/{id}")
	public ResponseEntity<Skills> getSkillById(@PathVariable int id) {
		Optional<Skills> skill = skillsService.getSkillsById(id);
		return skill.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar Skill (PUT: reemplazo completo)
	@Operation(summary = "Actualizar habilidad", description = "Reemplaza completamente la información de una habilidad")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Habilidad actualizada correctamente"),
		@ApiResponse(responseCode = "404", description = "Habilidad no encontrada")
	})
	@PutMapping("/{id}")
	public ResponseEntity<Skills> updateSkills(@PathVariable int id, @RequestBody Skills skillDetails) {
		Skills updatedSkill = skillsService.updateSkill(id, skillDetails);
		if (updatedSkill != null) {
			return new ResponseEntity<>(updatedSkill, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Actualizar parcialmente Skill (PATCH)
	@Operation(summary = "Actualizar parcialmente habilidad", description = "Modifica solo ciertos campos de una habilidad")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Habilidad actualizada correctamente"),
		@ApiResponse(responseCode = "404", description = "Habilidad no encontrada")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<Skills> patchSkills(@PathVariable int id, @RequestBody JsonNode skillUpdates) {
		Skills skillLog = skillsService.patchSkill(id, skillUpdates);
		if (skillLog != null) {
			return new ResponseEntity<>(skillLog, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar skill
	@Operation(summary = "Eliminar habilidad", description = "Elimina una habilidad existente por ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Habilidad eliminada correctamente"),
		@ApiResponse(responseCode = "404", description = "Habilidad no encontrada")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSkill(@PathVariable int id) {
		boolean deleted = skillsService.deleteSkill(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Obtener las skill por usuario
	@Operation(summary = "Obtener habilidades por usuario", description = "Devuelve todas las habilidades asociadas a un usuario de Oracle")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	@GetMapping("/oracleuser/{oracleUserId}")
	public ResponseEntity<List<Skills>> getSkillsByOracleUser(@PathVariable int oracleUserId) {
		List<Skills> skills = skillsService.getSkillsByOracleUser(oracleUserId);
		return new ResponseEntity<>(skills, HttpStatus.OK);
	}

}