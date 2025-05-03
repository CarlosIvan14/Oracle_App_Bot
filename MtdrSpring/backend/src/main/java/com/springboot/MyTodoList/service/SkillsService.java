package com.springboot.MyTodoList.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.Skills;
import com.springboot.MyTodoList.repository.SkillsRepository;

@Service
public class SkillsService {

	@Autowired
	private SkillsRepository skillsRepository;

	public Skills addSkills(Skills skills) {
		return skillsRepository.save(skills);
	}

	// Is this one really usefull in here?
	public List<Skills> findAllSkills() {
		return skillsRepository.findAll();
	}

	public Optional<Skills> getSkillsById(int id) {
		return skillsRepository.findById(id);
	}

	public Skills updateSkill(int id, Skills skillDetails) {
		return skillsRepository.findById(id).map(skill -> {
			// Actualización total (PUT)
			skill.setOracleUser(skill.getOracleUser());
			skill.setName(skill.getName());
			skill.setDescription(skill.getDescription());

			// Guardar los cambios en el repositorio
			return skillsRepository.save(skill);
		}).orElse(null);
	}

	public Skills patchSkill(int id, JsonNode timeLogUpdates) {
		return skillsRepository.findById(id).map(skill -> {
			ObjectMapper mapper = new ObjectMapper();
			// Para evitar fallos si se envían propiedades desconocidas
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				// Fusiona (merge) los campos enviados en taskUpdates sobre el objeto task
				// existente
				mapper.readerForUpdating(skill).readValue(timeLogUpdates);
			}
			catch (IOException e) {
				throw new RuntimeException("Error al actualizar el timelog", e);
			}
			return skillsRepository.save(skill);
		}).orElse(null);
	}

	public boolean deleteSkill(int id) {
		if (skillsRepository.existsById(id)) {
			skillsRepository.deleteById(id);
			return true;
		}
		return false;
	}

	// Método para obtener todos los timeLogs de un taskAssignee.
	public List<Skills> getSkillsByOracleUser(int oracleUserId) {
		return skillsRepository.findByOracleUserID(oracleUserId);
	}

}