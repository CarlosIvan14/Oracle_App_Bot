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

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.MyTodoList.model.Skills;
import com.springboot.MyTodoList.service.SkillsService;

@RestController
@RequestMapping("/api/skills")
public class SkillsController {

	@Autowired
	private SkillsService skillsService;

	// Crear Skill
	@PostMapping
	public ResponseEntity<Skills> createSkill(@RequestBody Skills skill) {
		Skills createdSkill = skillsService.addSkills(skill);
		return new ResponseEntity<>(createdSkill, HttpStatus.CREATED);
	}

	// Obtener todos los TimeLogs
	@GetMapping
	public ResponseEntity<List<Skills>> getAllSkills() {
		List<Skills> skills = skillsService.findAllSkills();
		return new ResponseEntity<>(skills, HttpStatus.OK);
	}

	// Obtener Skill por ID
	@GetMapping("/{id}")
	public ResponseEntity<Skills> getSkillById(@PathVariable int id) {
		Optional<Skills> skill = skillsService.getSkillsById(id);
		return skill.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Actualizar Skill (PUT: reemplazo completo)
	@PutMapping("/{id}")
	public ResponseEntity<Skills> updateSkills(@PathVariable int id, @RequestBody Skills skillDetails) {
		Skills updatedSkill = skillsService.updateSkill(id, skillDetails);
		if (updatedSkill != null) {
			return new ResponseEntity<>(updatedSkill, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Actualizar parcialmente Skill (PATCH)
	@PatchMapping("/{id}")
	public ResponseEntity<Skills> patchSkills(@PathVariable int id, @RequestBody JsonNode skillUpdates) {
		Skills skillLog = skillsService.patchSkill(id, skillUpdates);
		if (skillLog != null) {
			return new ResponseEntity<>(skillLog, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Eliminar TimeLog
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSkill(@PathVariable int id) {
		boolean deleted = skillsService.deleteSkill(id);
		if (deleted) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Obtener todos los timeLogs por taskAssignee.
	@GetMapping("/oracleuser/{oracleUserId}")
	public ResponseEntity<List<Skills>> getSkillsByOracleUser(@PathVariable int oracleUserId) {
		List<Skills> skills = skillsService.getSkillsByOracleUser(oracleUserId);
		return new ResponseEntity<>(skills, HttpStatus.OK);
	}

}