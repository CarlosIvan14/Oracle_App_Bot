package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.*;
import com.springboot.MyTodoList.service.ProjectsServiceBot;
import com.springboot.MyTodoList.service.SprintsServiceBot;
import com.springboot.MyTodoList.service.TaskServiceBot;
import com.springboot.MyTodoList.service.ToDoItemService;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    //services

    private final ProjectsServiceBot projectsServiceBot;
    private final SprintsServiceBot sprintsServiceBot;
    private final TaskServiceBot taskServiceBot;
    private final String baseUrl = "http://localhost:8081";
    private final RestTemplate restTemplate;
    private final String botName;
    private final String botToken;

    private enum Flow {
        NONE, LOGIN, ADD_SPRINT, ADD_TASK, ADD_USER, TASK_COMPLETE
    }

    /**
     * Estado de la conversación por chat.
     */
    private static class BotConversationState {
        Flow flow = Flow.NONE;
        int step = 0;
        OracleUser loggedUser;
        int currentProjectId;
        int currentSprintId;
        int currentTaskId;
        // Datos para login
        String newUserName;
        String newUserPassword;
        // Datos para crear sprint
        String newSprintName;
        // Datos para crear tarea
        String taskDescription;
        String taskDeadline; // formato "yyyy-MM-dd HH:mm" si lo requieres
        Integer taskPriority;
        // Datos para agregar usuario
        List<OracleUser> allOracleUsers;
    }

    private final Map<Long, BotConversationState> conversationStates = new HashMap<>();

    public ToDoItemBotController(String botToken, String botName, ProjectsServiceBot projectsServiceBot, SprintsServiceBot sprintsServiceBot, TaskServiceBot taskServiceBot) {
        super(botToken);
        this.botToken = botToken;
        this.botName = botName;
        this.restTemplate = new RestTemplate();
        this.projectsServiceBot = projectsServiceBot;
        this.sprintsServiceBot = sprintsServiceBot;
        this.taskServiceBot = taskServiceBot;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText().trim();
        BotConversationState state = conversationStates.computeIfAbsent(chatId, k -> new BotConversationState());

        // Comandos especiales
        if (messageText.equalsIgnoreCase("/start")) {
            startLoginFlow(chatId, state);
            return;
        }
        if (messageText.equalsIgnoreCase("/logout") || messageText.equalsIgnoreCase("Logout 🚪")) {
            logoutUser(chatId);
            return;
        }
        if (messageText.equalsIgnoreCase("/menu")) {
            if (state.loggedUser != null) {
                showMainMenu(chatId, state.loggedUser);
            } else {
                sendMsg(chatId, "No hay sesión activa. Usa /start para loguearte.", false);
            }
            return;
        }
        // Flujo para ver usuarios
        if (messageText.startsWith("👥 Ver Usuarios Proyecto")) {
            int projectId = extractProjectIdFromUsersCommand(messageText);
            if (projectId != -1) {
                showUsersForProject(chatId, projectId);
                return;
            }
        }
        // Flujo para agregar usuario
        if (messageText.equalsIgnoreCase("➕ Agregar Usuario")) {
            state.flow = Flow.ADD_USER;
            state.step = 1;
            fetchAndShowAllOracleUsers(chatId, state);
            return;
        }
        // Navegación: volver a Proyectos o Sprints
        if (handleNavigation(chatId, messageText, state)) return;

        if (state.loggedUser == null && state.flow != Flow.LOGIN) {
            startLoginFlow(chatId, state);
            return;
        }
        if (state.flow != Flow.NONE) {
            processFlow(chatId, messageText, state);
            return;
        }
        if (handleSprintSelection(chatId, messageText, state)) return;
        if (handleProjectSelection(chatId, messageText, state)) return;
        // 10. Handle task status updates
        if (handleTaskStatusUpdates(chatId, messageText, state)){

        }

    }

    private int extractProjectIdFromUsersCommand(String messageText) {
        try {
            String[] parts = messageText.split(" ");
            String idStr = parts[parts.length - 1];
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            logger.error("Error extrayendo projectId de comando 'Ver Usuarios Proyecto'", e);
            return -1;
        }
    }

    // --------------------------
    // Flujo de Login
    // --------------------------
    private void startLoginFlow(long chatId, BotConversationState state) {
        state.flow = Flow.LOGIN;
        state.step = 1;
        sendMsg(chatId, "¡Bienvenido! Ingresa tu *nombre de usuario*:", true);
    }
    private void processFlow(long chatId, String messageText, BotConversationState state) {
        switch (state.flow) {
            case LOGIN:
                processLoginFlow(chatId, messageText, state);
                break;
            case ADD_SPRINT:
                processAddSprintFlow(chatId, messageText, state);
                break;
            case ADD_TASK:
                //processAddTaskFlow(chatId, messageText, state);
                break;
            case TASK_COMPLETE:
                if (state.step == 1) {
                    try {
                        double hours = Double.parseDouble(messageText);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "COMPLETED");
                        updates.put("realHours", hours);
                        
                        taskServiceBot.updateTask(state.currentTaskId, updates);
                        listTasksForSprint(chatId, state.currentSprintId, state.loggedUser.getIdUser());
                        resetFlow(state);
                    } catch (NumberFormatException e) {
                        sendMsg(chatId, "⚠ Por favor ingresa un número válido (ej. 2.5)", false);
                    }
                }
                break;
            case ADD_USER:
                processAddUserFlow(chatId, messageText, state);
                break;
            default:
                break;
        }
    }
    private void processLoginFlow(long chatId, String messageText, BotConversationState state) {
        if (state.step == 1) {
            state.newUserName = messageText;
            state.step = 2;
            sendMsg(chatId, "Ahora ingresa tu *password*:", true);
        } else if (state.step == 2) {
            state.newUserPassword = messageText;
            OracleUser user = doLogin(state.newUserName, state.newUserPassword);
            if (user == null) {
                sendMsg(chatId, "Login fallido. Intenta nuevamente con /start.", false);
                conversationStates.remove(chatId);
            } else {
                state.loggedUser = user;
                resetFlow(state);
                sendMsg(chatId, "¡Login exitoso! Bienvenido, " + user.getName(), false);
                showMainMenu(chatId, user);
            }
        }
    }

    // --------------------------
    // Menú Principal: Proyectos
    // --------------------------
    private void showMainMenu(long chatId, OracleUser user) {
        List<Projects> projects = getProjectsForUser(user.getIdUser());
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow titleRow = new KeyboardRow();
        titleRow.add("== Proyectos Asignados ==");
        rows.add(titleRow);

        if (projects.isEmpty()) {
            KeyboardRow row = new KeyboardRow();
            row.add("No tienes proyectos asignados");
            rows.add(row);
        } else {
            for (Projects p : projects) {
                KeyboardRow row = new KeyboardRow();
                row.add("📁 " + p.getName() + " (ID: " + p.getIdProject() + ")");
                rows.add(row);
            }
        }
        KeyboardRow logoutRow = new KeyboardRow();
        logoutRow.add("Logout 🚪");
        rows.add(logoutRow);

        keyboard.setKeyboard(rows);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Menú Principal:");
        msg.setReplyMarkup(keyboard);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error en showMainMenu", e);
        }
    }

    private boolean handleProjectSelection(long chatId, String messageText, BotConversationState state) {
        Pattern pattern = Pattern.compile("\\(ID: (\\d+)\\)");
        Matcher matcher = pattern.matcher(messageText);
        if (matcher.find()) {
            int projectId = Integer.parseInt(matcher.group(1));
            state.currentProjectId = projectId;
            boolean isManager = isManager(state.loggedUser.getIdUser(), projectId);
            showSprintsForProject(chatId, projectId, isManager);
            return true;
        }
        return false;
    }
    private boolean handleSprintSelection(long chatId, String messageText, BotConversationState state) {
        // Check if the message contains #SPRINT#
        if (messageText.contains("#SPRINT#")) {
            // Extract the sprint ID from the message (using regex to find (ID: <number>))
            Matcher sprintMatcher = Pattern.compile(".*\\(ID: (\\d+)\\) #SPRINT#").matcher(messageText);
            if (sprintMatcher.find()) {
                int sprintId = Integer.parseInt(sprintMatcher.group(1));
                state.currentSprintId = sprintId; // Set the selected sprint ID in the conversation state
                listTasksForSprint(chatId, sprintId, state.loggedUser.getIdUser()); // Call the function to show tasks related to the sprint
                return true;
            }
        }
        return false;
    }
    private boolean handleTaskStatusUpdates(long chatId, String messageText, BotConversationState state) {
        // Check for START (Assigned → In Progress)
        if (messageText.startsWith("▶ START-")) {
            int taskId = Integer.parseInt(messageText.replace("▶ START-", ""));
            updateTaskStatus(chatId, taskId, "IN_PROGRESS", state.currentSprintId);
            return true;
        }
        // Check for DONE (In Progress → Completed)
        else if (messageText.startsWith("✅ DONE-")) {
            int taskId = Integer.parseInt(messageText.replace("✅ DONE-", ""));
            state.flow = Flow.TASK_COMPLETE;
            state.step = 1;
            state.currentTaskId = taskId;
            sendMsg(chatId, "⌛ Por favor ingresa las horas trabajadas (ej. 2.5):", false);
            return true;
        }
        // Check for CANCEL (In Progress → Assigned)
        else if (messageText.startsWith("❌ CANCEL-")) {
            int taskId = Integer.parseInt(messageText.replace("❌ CANCEL-", ""));
            updateTaskStatus(chatId, taskId, "ASSIGNED", state.currentSprintId);
            return true;
        }
        // Check for UNDO (Completed → In Progress) 
        else if (messageText.startsWith("↩ UNDO-")) {
            int taskId = Integer.parseInt(messageText.replace("↩ UNDO-", ""));
            updateTaskStatus(chatId, taskId, "IN_PROGRESS", state.currentSprintId);
            return true;
        }
        return false;
    }
    private void updateTaskStatus(long chatId, int taskId, String newStatus, int sprintId) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus);
            
            // Add realHours if completing the task
            if ("COMPLETED".equals(newStatus)) {
                // You might want to ask for hours worked here
                updates.put("realHours", 0.0); // Default or prompt user
            }
            
            taskServiceBot.updateTask(taskId, updates);
            
            // Refresh the task list
            listTasksForSprint(chatId, sprintId, conversationStates.get(chatId).loggedUser.getIdUser());
            
            sendMsg(chatId, "✅ Estado de tarea actualizado a: " + newStatus, false);
        } catch (Exception e) {
            logger.error("Error updating task status", e);
            sendMsg(chatId, "❌ Error al actualizar la tarea", false);
        }
    }
    // --------------------------
    // Mostrar Sprints de un Proyecto
    // --------------------------
    private void showSprintsForProject(long chatId, int projectId, boolean isManager) {
        List<Sprint> sprints = sprintsServiceBot.getSprintsByProjectId(projectId);
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow backRow = new KeyboardRow();
        backRow.add("⬅️ Volver a Proyectos");
        rows.add(backRow);

        // Solo si el usuario es manager se muestran los botones "Ver Usuarios" y "Añadir Sprint"
        if (isManager) {
            KeyboardRow usersRow = new KeyboardRow();
            usersRow.add("👥 Ver Usuarios Proyecto " + projectId);
            rows.add(usersRow);
            KeyboardRow addSprintRow = new KeyboardRow();
            addSprintRow.add("➕ Añadir Sprint");
            rows.add(addSprintRow);
        }

        KeyboardRow titleRow = new KeyboardRow();
        titleRow.add("Sprints del Proyecto " + projectId);
        rows.add(titleRow);

        for (Sprint sprint : sprints) {
            KeyboardRow row = new KeyboardRow();
            String statusIcon = "Activo".equals(sprint.getDescription()) ? "🟢" : "🔴"; // Adjust based on actual status field
            // Add a special tag like #SPRINT# to identify that this is a sprint
            String sprintText = statusIcon + " " + sprint.getName() + " (ID: " + sprint.getId() + ") #SPRINT#";
            row.add(sprintText);
            rows.add(row);
}
        keyboard.setKeyboard(rows);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Selecciona un sprint:");
        msg.setReplyMarkup(keyboard);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error en showSprintsForProject", e);
        }
    }
    private void listTasksForSprint(long chatId, int sprintId, int userId) {
        // Fetch TaskAssignees dynamically
        List<TaskAssignees> taskAssignments = taskServiceBot.getUserTaskAssignments(sprintId, userId);
    
        List<Tasks> tasks = taskAssignments.stream()
        .map(TaskAssignees::getTask)
        .collect(Collectors.toList());
    
        // Filtrar tareas por estado
        List<Tasks> assigned = tasks.stream()
                .filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
        List<Tasks> inProgress = tasks.stream()
                .filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
        List<Tasks> completed = tasks.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
    
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
    
        // Navigation buttons
        KeyboardRow headerRow = new KeyboardRow();
        headerRow.add("⬅ Volver a Sprints");
        rows.add(headerRow);
    
        // Button to add a new task
        KeyboardRow addTaskRow = new KeyboardRow();
        addTaskRow.add("➕ Add Task");
        rows.add(addTaskRow);
    
        // Assigned Section
        if (!assigned.isEmpty()) {
            rows.add(createTitleRow("==📥 ASIGNADAS 📥=="));
            assigned.forEach(task -> {
                KeyboardRow row = new KeyboardRow();
                row.add(task.getDescription() + " [ID: " + task.getId() + "]");
                row.add("▶ START-" + task.getId());
                rows.add(row);
            });
        }
    
        // In Progress Section
        if (!inProgress.isEmpty()) {
            rows.add(createTitleRow("==⏳ EN PROGRESO ⏳=="));
            inProgress.forEach(task -> {
                KeyboardRow row = new KeyboardRow();
                row.add(task.getDescription() + " [ID: " + task.getId() + "]");
                row.add("❌ CANCEL-" + task.getId());
                row.add("✅ DONE-" + task.getId());
                rows.add(row);
            });
        }
    
        // Completed Section
        if (!completed.isEmpty()) {
            rows.add(createTitleRow("==✅ COMPLETADAS ✅=="));
            completed.forEach(task -> {
                KeyboardRow row = new KeyboardRow();
                row.add(task.getDescription() + " [ID: " + task.getId() + "]");
                row.add("↩ UNDO-" + task.getId());
                rows.add(row);
            });
        }
    
        keyboard.setKeyboard(rows);
    
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Tablero del Sprint " + sprintId);
        msg.setReplyMarkup(keyboard);
    
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // --------------------------
    // Listar Tareas del Sprint
    // --------------------------
    private KeyboardRow createTitleRow(String title) {
        KeyboardRow row = new KeyboardRow();
        row.add(title);
        return row;
    }
    

    // --------------------------
    // Flujo: Crear Sprint
    // --------------------------
    private void processAddSprintFlow(long chatId, String messageText, BotConversationState state) {
        if (state.step == 1) {
            state.newSprintName = messageText;
            state.step = 2;
            sendMsg(chatId, "Confirma el nuevo sprint con el nombre: *" + state.newSprintName + "*.\n" +
                    "Escribe `/confirmar` para proceder o `/cancel` para cancelar.", true);
        } else if (state.step == 2) {
            if (messageText.equalsIgnoreCase("/confirmar")) {
                Sprint sprint = new Sprint();
                sprint.setName(state.newSprintName);
                sprint.setDescription("Activo");
                Projects p = new Projects();
                p.setIdProject(state.currentProjectId);
                sprint.setProject(p);
                Sprint created = createSprint(sprint);
                if (created != null) {
                    sendMsg(chatId, "Sprint creado: " + created.getName() + " (ID: " + created.getId() + ")", false);
                } else {
                    sendMsg(chatId, "Error al crear el sprint.", false);
                }
                resetFlow(state);
                boolean isManager = isManager(state.loggedUser.getIdUser(), state.currentProjectId);
                showSprintsForProject(chatId, state.currentProjectId, isManager);
            } else if (messageText.equalsIgnoreCase("/cancel")) {
                sendMsg(chatId, "Creación de sprint cancelada.", false);
                resetFlow(state);
                boolean isManager = isManager(state.loggedUser.getIdUser(), state.currentProjectId);
                showSprintsForProject(chatId, state.currentProjectId, isManager);
            }
        }
    }

    
    // --------------------------
    // Flujo: Agregar Usuario al Proyecto
    // --------------------------
    private void fetchAndShowAllOracleUsers(long chatId, BotConversationState state) {
        List<OracleUser> allUsers = getAllOracleUsers();
        state.allOracleUsers = allUsers;
        StringBuilder sb = new StringBuilder("*Lista de Usuarios Disponibles:*\n\n");
        for (int i = 0; i < allUsers.size(); i++) {
            OracleUser u = allUsers.get(i);
            sb.append(i + 1).append(") ").append(u.getName()).append(" (ID: ").append(u.getIdUser()).append(")\n");
        }
        sb.append("\nIngresa el número del usuario que deseas agregar al proyecto.");
        sendMsg(chatId, sb.toString(), true);
    }
    
    private void processAddUserFlow(long chatId, String messageText, BotConversationState state) {
        try {
            int index = Integer.parseInt(messageText.trim());
            if (state.allOracleUsers != null && index > 0 && index <= state.allOracleUsers.size()) {
                OracleUser selected = state.allOracleUsers.get(index - 1);
                // Construir payload con la estructura requerida:
                Map<String, Object> payload = new HashMap<>();
                Map<String, Object> projectMap = new HashMap<>();
                projectMap.put("id_project", state.currentProjectId);
                payload.put("project", projectMap);
                payload.put("roleUser", "developer"); // Se crea como developer
                payload.put("status", "active");
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("idUser", selected.getIdUser());
                payload.put("user", userMap);

                addUserToProject(payload);
                sendMsg(chatId, "Usuario agregado exitosamente.", false);
                showUsersForProject(chatId, state.currentProjectId);
                resetFlow(state);
            } else {
                sendMsg(chatId, "Número inválido. Intenta nuevamente:", false);
            }
        } catch (NumberFormatException e) {
            sendMsg(chatId, "Entrada inválida. Ingresa el número del usuario que deseas agregar:", false);
        }
    }

    // --------------------------
    // Mostrar Usuarios del Proyecto
    // --------------------------
    private void showUsersForProject(long chatId, int projectId) {
        try {
            String url = baseUrl + "/api/project-users/project/" + projectId + "/users";
            ResponseEntity<OracleUser[]> resp = restTemplate.getForEntity(url, OracleUser[].class);
            OracleUser[] users = resp.getBody();
            StringBuilder sb = new StringBuilder("*Usuarios en el Proyecto " + projectId + ":*\n\n");
            if (users != null && users.length > 0) {
                for (OracleUser u : users) {
                    sb.append("🆔 ").append(u.getIdUser()).append(" - ").append(u.getName()).append("\n");
                }
            } else {
                sb.append("No hay usuarios asignados al proyecto.");
            }
            // Solo si el usuario es manager se muestra "➕ Agregar Usuario"
            boolean isManager = isManager(conversationStates.get(chatId).loggedUser.getIdUser(), projectId);
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);
            List<KeyboardRow> rows = new ArrayList<>();
            if (isManager) {
                KeyboardRow addUserRow = new KeyboardRow();
                addUserRow.add("➕ Agregar Usuario");
                rows.add(addUserRow);
            }
            KeyboardRow backRow = new KeyboardRow();
            backRow.add("⬅️ Volver a Sprints");
            rows.add(backRow);
            keyboard.setKeyboard(rows);
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(sb.toString());
            msg.setReplyMarkup(keyboard);
            msg.enableMarkdown(true);
            execute(msg);
        } catch (Exception e) {
            logger.error("Error en showUsersForProject", e);
            sendMsg(chatId, "Error al obtener usuarios.", false);
        }
    }

    // --------------------------
    // LOGOUT y Navegación
    // --------------------------
    private void logoutUser(long chatId) {
        conversationStates.remove(chatId);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Sesión cerrada. Usa /start para ingresar de nuevo.");
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error en logoutUser", e);
        }
    }
    private boolean handleNavigation(long chatId, String messageText, BotConversationState state) {
        if (messageText.equals("⬅️ Volver a Proyectos") || messageText.equals("⬅️ Regresar a Proyectos")) {
            showMainMenu(chatId, state.loggedUser);
            return true;
        }
        if (messageText.equals("⬅️ Volver a Sprints")) {
            boolean isManager = isManager(state.loggedUser.getIdUser(), state.currentProjectId);
            showSprintsForProject(chatId, state.currentProjectId, isManager);
            return true;
        }
        return false;
    }
    private void resetFlow(BotConversationState state) {
        state.flow = Flow.NONE;
        state.step = 0;
        state.newSprintName = null;
        state.taskDescription = null;
        state.taskDeadline = null;
        state.taskPriority = null;
        state.allOracleUsers = null;
    }
    private void sendMsg(long chatId, String text, boolean markdown) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        if (markdown) msg.enableMarkdown(true);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error al enviar mensaje", e);
        }
    }

    // --------------------------
    // Llamadas al Backend
    // --------------------------
    // 1) Login (/users/login)
    private OracleUser doLogin(String username, String password) {
        try {
            String url = baseUrl + "/users/login";
            LoginRequest req = new LoginRequest();
            req.setName(username);
            req.setPassword(password);
            ResponseEntity<OracleUser> resp = restTemplate.postForEntity(url, req, OracleUser.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                return resp.getBody();
            }
        } catch (Exception e) {
            logger.error("Error en doLogin", e);
        }
        return null;
    }

    // 2) Obtener proyectos para un usuario (/api/project-users/user/{userId}/projects)
    private List<Projects> getProjectsForUser(int userId) {
        try {
            String url = baseUrl + "/api/project-users/user/" + userId + "/projects";
            ResponseEntity<Projects[]> resp = restTemplate.getForEntity(url, Projects[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return Arrays.asList(resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Error en getProjectsForUser", e);
        }
        return Collections.emptyList();
    }


    // 7) Crear un sprint (POST /api/sprints)
    private Sprint createSprint(Sprint sprint) {
        try {
            String url = baseUrl + "/api/sprints";
            ResponseEntity<Sprint> resp = restTemplate.postForEntity(url, sprint, Sprint.class);
            if (resp.getStatusCode() == HttpStatus.CREATED && resp.getBody() != null) {
                return resp.getBody();
            }
        } catch (Exception e) {
            logger.error("Error en createSprint", e);
        }
        return null;
    }


    // 10) Obtener todos los OracleUser (/users)
    private List<OracleUser> getAllOracleUsers() {
        try {
            String url = baseUrl + "/users";
            ResponseEntity<OracleUser[]> resp = restTemplate.getForEntity(url, OracleUser[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return Arrays.asList(resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Error en getAllOracleUsers", e);
        }
        return Collections.emptyList();
    }

    // 11) Agregar un usuario a un proyecto (POST /api/project-users)
    // Se envía el payload en forma de Map con la estructura requerida.
    private void addUserToProject(Map<String, Object> payload) {
        try {
            String url = baseUrl + "/api/project-users";
            restTemplate.postForEntity(url, payload, Object.class);
        } catch (Exception e) {
            logger.error("Error en addUserToProject", e);
        }
    }
    
    // --------------------------
    // Método isManager: verifica si un usuario es manager en un proyecto.
    // --------------------------
    private boolean isManager(int userId, int projectId) {
        try {
            String url = baseUrl + "/api/project-users";
            ResponseEntity<ProjectUser[]> resp = restTemplate.getForEntity(url, ProjectUser[].class);
            if (resp.getBody() != null) {
                for (ProjectUser pu : resp.getBody()) {
                    if (pu.getUser() != null 
                        && pu.getUser().getIdUser() == userId 
                        && pu.getProject() != null 
                        && pu.getProject().getIdProject() == projectId 
                        && pu.getRoleUser() != null 
                        && pu.getRoleUser().trim().equalsIgnoreCase("manager")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error en isManager", e);
        }
        return true;
    }

}
