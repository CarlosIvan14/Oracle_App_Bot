package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.Skills;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Dado: • projectId • nombre y descripción de la tarea
 *
 * Devuelve la lista de usuarios que PERTENECEN al proyecto, ordenados de mejor → peor
 * match según sus skills.
 */
@Service
public class OpenAIService {

	/* ========== configuración ========== */
	@Value("${openai.api.key}")
	private String openaiApiKey;

	@Value("${backend.base-url:http://140.84.170.68}")
	private String backendBaseUrl; // <‑‑ configurable

	private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

	/* ========== dependencias ========== */
	private final SkillsService skillsService;

	private final RestTemplate restTemplate = new RestTemplate();

	public OpenAIService(SkillsService skillsService) {
		this.skillsService = skillsService;
	}

	/* ========================================================= */
	public List<OracleUser> rankUsersForTask(int projectId, String taskName, String taskDescription) {

		/* 1) Traer SOLO los usuarios del proyecto */
		String urlUsers = backendBaseUrl + "/api/project-users/project/" + projectId + "/users";
		ResponseEntity<OracleUser[]> respUsers = restTemplate.getForEntity(urlUsers, OracleUser[].class);

		OracleUser[] projectUsersArr = respUsers.getBody();
		if (projectUsersArr == null || projectUsersArr.length == 0)
			return Collections.emptyList();

		List<OracleUser> projectUsers = Arrays.asList(projectUsersArr);

		/* 2) skills por usuario */
		Map<Integer, List<Skills>> skillsByUser = new HashMap<>();
		for (OracleUser u : projectUsers) {
			skillsByUser.put(u.getIdUser(), skillsService.getSkillsByOracleUser(u.getIdUser()));
		}

		/* 3) Construir prompt */
		StringBuilder prompt = new StringBuilder();
		prompt.append("Tienes que asignar una tarea en un proyecto de software.\n").append("Nombre de la tarea: ")
				.append(taskName).append('\n').append("Descripción de la tarea: ").append(taskDescription)
				.append("\n\n")
				.append("A continuación la lista de usuarios disponibles en el proyecto y sus skills:\n");

		for (OracleUser u : projectUsers) {
			prompt.append("ID=").append(u.getIdUser()).append(", Nombre=").append(u.getName()).append('\n');

			List<Skills> ss = skillsByUser.get(u.getIdUser());
			if (ss.isEmpty()) {
				prompt.append("  • (sin skills registradas)\n");
			}
			else {
				for (Skills s : ss) {
					prompt.append("  • ").append(s.getName()).append(" → ").append(s.getDescription()).append('\n');
				}
			}
		}

		prompt.append("\nOrdena **todos** los IDs anteriores de mejor a peor match.\n")
				.append("Devuelve **solo** la lista separada por comas, sin texto extra.");

		/* 4) Llamada a OpenAI */
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(openaiApiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> body = new HashMap<>();
		body.put("model", "gpt-4o");
		body.put("temperature", 0);
		body.put("max_tokens", 50);
		body.put("messages", List.of(Map.of("role", "user", "content", prompt.toString())));

		ResponseEntity<Map> respOA = restTemplate.postForEntity(OPENAI_URL, new HttpEntity<>(body, headers), Map.class);

		if (!respOA.getStatusCode().is2xxSuccessful())
			throw new RuntimeException("OpenAI error " + respOA.getStatusCode());

		String content = Optional.ofNullable(respOA.getBody()).map(m -> (List<?>) m.get("choices"))
				.filter(l -> !l.isEmpty()).map(l -> (Map<?, ?>) l.get(0)).map(c -> (Map<?, ?>) c.get("message"))
				.map(m -> (String) m.get("content")).orElseThrow(() -> new RuntimeException("Respuesta vacía OpenAI"));

		/* 5) Parsear IDs */
		List<Integer> orderedIds = new ArrayList<>();
		for (String part : content.split(",")) {
			try {
				orderedIds.add(Integer.parseInt(part.trim()));
			}
			catch (NumberFormatException ignore) {
			}
		}

		/* 6) Construir lista ordenada de usuarios */
		List<OracleUser> ordered = new ArrayList<>();
		for (Integer id : orderedIds) {
			projectUsers.stream().filter(u -> u.getIdUser() == id).findFirst().ifPresent(ordered::add);
		}
		/* por seguridad incluir los que falten */
		for (OracleUser u : projectUsers)
			if (!ordered.contains(u))
				ordered.add(u);

		return ordered;
	}

}
