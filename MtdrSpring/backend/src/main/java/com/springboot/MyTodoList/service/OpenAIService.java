package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey; // Configura este valor en application.properties o mediante .env

    private final OracleUserService oracleUserService;

    public OpenAIService(OracleUserService oracleUserService) {
        this.oracleUserService = oracleUserService;
    }

    public List<OracleUser> getAssignedUsers(String taskDescription) {
        // Obtiene todos los usuarios con sus campos relevantes
        List<OracleUser> allUsers = oracleUserService.getAllUsers();

        // Construir el prompt para OpenAI
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tarea: ").append(taskDescription).append("\n");
        prompt.append("Lista de usuarios (ID, Role, Name, Skill):\n");
        for (OracleUser user : allUsers) {
            prompt.append("ID: ").append(user.getIdUser())
                  .append(", Role: ").append(user.getRole())
                  .append(", Name: ").append(user.getName())
                  .append(", Skill: ").append(user.getSkill())
                  .append("\n");
        }
        // Solicitar una salida precisa: una lista de IDs separados por coma sin texto adicional
        prompt.append("Ordena los usuarios de mejor a peor perfil basándote en el usuario que tenga las skills mas adecuadas de acorde a la descripción de la tarea y si no tienen parecido, entonces ordenalas en base solo a las skills.y");
        prompt.append("Devuelve únicamente una lista de IDs (sin espacios ni texto adicional), separados por comas y no omitas ningun usuario ordenalos todos.");

        // Preparar la llamada a la API de OpenAI utilizando el endpoint de chat completions
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // El cuerpo de la solicitud ahora debe tener "messages"
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        // Aumentamos max_tokens y configuramos temperature para respuestas más deterministas
        requestBody.put("max_tokens", 250);
        requestBody.put("temperature", 0);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt.toString());
        messages.add(userMessage);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al llamar a OpenAI");
        }

        // Extraer la respuesta del endpoint de chat completions
        List choices = (List) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("Respuesta vacía de OpenAI");
        }
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");
        String textResponse = (String) message.get("content");
        
        // Si la respuesta está vacía, imprime el prompt y la respuesta para depuración
        if (textResponse == null || textResponse.trim().isEmpty()) {
            throw new RuntimeException("La respuesta de OpenAI está vacía. Prompt enviado: " + prompt.toString());
        }

        // Parsear la respuesta para obtener los IDs
        List<Long> sortedIds = new ArrayList<>();
        for (String s : textResponse.split(",")) {
            try {
                sortedIds.add(Long.parseLong(s.trim()));
            } catch (NumberFormatException e) {
                // Se ignoran valores no numéricos, también podrías loguear el error para depuración
            }
        }

        // Construir la lista de usuarios ordenada según los IDs devueltos
        List<OracleUser> sortedUsers = new ArrayList<>();
        for (Long id : sortedIds) {
            for (OracleUser user : allUsers) {
                if (user.getIdUser() == id.intValue()) {
                    sortedUsers.add(user);
                    break;
                }
            }
        }
        return sortedUsers;
    }
}
