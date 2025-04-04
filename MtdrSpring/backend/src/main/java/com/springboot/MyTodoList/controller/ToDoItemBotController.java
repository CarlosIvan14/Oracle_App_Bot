package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.*;
import com.springboot.MyTodoList.service.ProjectsServiceBot;
import com.springboot.MyTodoList.service.SprintsServiceBot;
import com.springboot.MyTodoList.service.TaskCreationServiceBot;
import com.springboot.MyTodoList.service.TaskServiceBot;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.UserRoleServiceBot;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    //services

    private final ProjectsServiceBot projectsServiceBot;
    private final SprintsServiceBot sprintsServiceBot;
    private final TaskServiceBot taskServiceBot;
    private final TaskCreationServiceBot taskCreationServiceBot;
    private final UserRoleServiceBot userRoleServiceBot;
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
        // Task creation fields
        private String taskName;
        private String taskDescription;
        private LocalDate taskDeadline;
        private Integer taskStoryPoints;
        private Double taskEstimatedHours;
        // Datos para agregar usuario
        List<OracleUser> allOracleUsers;
    }

    private final Map<Long, BotConversationState> conversationStates = new HashMap<>();

    public ToDoItemBotController(String botToken, String botName, ProjectsServiceBot projectsServiceBot, SprintsServiceBot sprintsServiceBot, TaskServiceBot taskServiceBot, TaskCreationServiceBot taskCreationServiceBot, UserRoleServiceBot userRoleServiceBot) {
        super(botToken);
        this.botToken = botToken;
        this.botName = botName;
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        this.projectsServiceBot = projectsServiceBot;
        this.sprintsServiceBot = sprintsServiceBot;
        this.taskServiceBot = taskServiceBot;
        this.taskCreationServiceBot = taskCreationServiceBot;
        this.userRoleServiceBot = userRoleServiceBot;
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
        // Flujo para agregar usuario
        if (messageText.equalsIgnoreCase("➕ Añadir Sprint")) {
            state.step = 1;
            state.flow = Flow.ADD_SPRINT;
            sendMsg(chatId, "Por favor ingresa el nombre del nuevo sprint:", true);
            return;
        }        
        if (messageText.equals("📊 Reports")) {
            showReports(chatId, state);
            return;
        }
        if (messageText.startsWith("Deshabilitar-")) {
            int sprintId = Integer.parseInt(messageText.replace("Deshabilitar-", ""));
            updateSprintStatus(chatId, sprintId, "idle");
            return;
        } else if (messageText.startsWith("Habilitar-")) {
            int sprintId = Integer.parseInt(messageText.replace("Habilitar-", ""));
            updateSprintStatus(chatId, sprintId, "Active");
            return;
        }                

        // Add this with your other command handlers
        if (messageText.equals("➕ Add Task")) {
            startAddTaskFlow(chatId, state);
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
    private void updateSprintStatus(long chatId, int sprintId, String newStatus) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", newStatus);
            String url = baseUrl + "/api/sprints/" + sprintId;
            ResponseEntity<Sprint> resp = restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(updates), Sprint.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                sendMsg(chatId, "Sprint actualizado a: " + newStatus, false);
            } else {
                sendMsg(chatId, "Error al actualizar el sprint.", false);
            }
        } catch (Exception e) {
            logger.error("Error updating sprint status", e);
            sendMsg(chatId, "Error al actualizar el sprint.", false);
        }
        // Refrescar la lista de sprints
        boolean isManager = isManager(conversationStates.get(chatId).loggedUser.getIdUser(), conversationStates.get(chatId).currentProjectId);
        showSprintsForProject(chatId, conversationStates.get(chatId).currentProjectId, isManager);
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
                processAddTaskFlow(chatId, messageText, state);
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

    private void startAddTaskFlow(long chatId, BotConversationState state) {
    if (state.currentSprintId == 0) {
        sendMsg(chatId, "⚠️ Please select a sprint first", false);
        return;
    }
    
    state.flow = Flow.ADD_TASK;
    state.step = 1;
    sendMsg(chatId, "🆕 Creating new task\n1) Enter task name:", false);
}

private void processAddTaskFlow(long chatId, String messageText, BotConversationState state) {
    switch (state.step) {
        case 1: // Task name
            state.taskName = messageText;
            state.step = 2;
            sendMsg(chatId, "2) Enter task description:", false);
            break;
            
        case 2: // Description
            state.taskDescription = messageText;
            state.step = 3;
            sendMsg(chatId, "3) Enter deadline (YYYY-MM-DD):", false);
            break;
            
        case 3: // Deadline
            try {
                state.taskDeadline = LocalDate.parse(messageText);
                state.step = 4;
                sendMsg(chatId, "4) Enter story points (integer):", false);
            } catch (DateTimeParseException e) {
                sendMsg(chatId, "⚠️ Invalid date format. Please use YYYY-MM-DD", false);
            }
            break;
            
        case 4: // Story points
            try {
                state.taskStoryPoints = Integer.parseInt(messageText);
                state.step = 5;
                sendMsg(chatId, "5) Enter estimated hours (e.g., 2.5):", false);
            } catch (NumberFormatException e) {
                sendMsg(chatId, "⚠️ Please enter a whole number", false);
            }
            break;
            
        case 5: // Estimated hours
            try {
                state.taskEstimatedHours = Double.parseDouble(messageText);
                
                // Create and send the task
                createAndSendTask(chatId, state);
                
                // Reset flow
                resetFlow(state);
                // Refresh task list
                listTasksForSprint(chatId, state.currentSprintId, state.loggedUser.getIdUser());
                
            } catch (NumberFormatException e) {
                sendMsg(chatId, "⚠️ Please enter a valid number (e.g., 2.5)", false);
            }
            break;
    }
}

private Sprint createSprintWithId(int sprintId) {
    Sprint sprint = new Sprint();
    sprint.setId(sprintId); // Assuming Sprint has setId() method
    return sprint;
}

private void createAndSendTask(long chatId, BotConversationState state) {

    logger.debug("state currentSprint", state.currentSprintId);
    try {
        // 1. Create task object
        Tasks newTask = new Tasks();
        newTask.setName(state.taskName);
        newTask.setDescription(state.taskDescription);
        newTask.setDeadline(state.taskDeadline.atStartOfDay());
        newTask.setStoryPoints(state.taskStoryPoints);
        newTask.setEstimatedHours(state.taskEstimatedHours);
        newTask.setRealHours(0.0);
        newTask.setCreationTs(LocalDateTime.now());
        newTask.setSprint(createSprintWithId(state.currentSprintId));

        // Create sprint object with correct field name
        logger.debug("state currentSprint", state.currentSprintId);
        Sprint sprint = new Sprint();
        sprint.setId(state.currentSprintId); 
        newTask.setSprint(sprint);

        // 2. Debug print before setting status
        logger.debug("Task object before status assignment: {}", newTask);
        
        // 3. Set status based on role
        boolean isManager = userRoleServiceBot.isManagerInProject(
            state.currentProjectId, 
            state.loggedUser.getIdUser()
        );
        newTask.setStatus(isManager ? "UNASSIGNED" : "ASSIGNED");
        
        // 4. Debug print after status assignment
        logger.debug("Task object after status assignment: {}", newTask);
        logger.debug("User is {} in project {}", 
            isManager ? "manager" : "developer", 
            state.currentProjectId);

        // 5. Create the task
        Tasks createdTask = taskCreationServiceBot.createTask(newTask);
        logger.debug("Task created with ID: {}", createdTask.getId());
        
        // 4. Auto-assign if developer
        if (!isManager) {
            Integer projectUserId = taskCreationServiceBot.getProjectUserId(
                state.currentProjectId,
                state.loggedUser.getIdUser()
            );
            if (projectUserId != null) {
                taskCreationServiceBot.assignTask(createdTask.getId(), projectUserId);
                logger.debug("Task assigned to project user ID: {}", projectUserId);
            }
        }
        
        // 7. Final debug output
        logger.debug("Final task state: {}", createdTask);
        sendMsg(chatId, "✅ Task created successfully!", false);
    } catch (Exception e) {
        logger.error("Error creating task. State: {}, Error: {}", state, e.getMessage(), e);
        sendMsg(chatId, "❌ Error creating task: " + e.getMessage(), false);
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
                    // Set status based on role
        boolean isManager = userRoleServiceBot.isManagerInProject(
            state.currentProjectId, 
            state.loggedUser.getIdUser()
        );
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
    
        // Botón de navegación
        KeyboardRow backRow = new KeyboardRow();
        backRow.add("⬅️ Volver a Proyectos");
        rows.add(backRow);
    
        // Botones solo para manager
        if (isManager) {
            KeyboardRow usersRow = new KeyboardRow();
            usersRow.add("👥 Ver Usuarios Proyecto " + projectId);
            rows.add(usersRow);
            KeyboardRow addSprintRow = new KeyboardRow();
            addSprintRow.add("➕ Añadir Sprint");
            rows.add(addSprintRow);
        }
    
        // Título
        KeyboardRow titleRow = new KeyboardRow();
        titleRow.add("Sprints del Proyecto " + projectId);
        rows.add(titleRow);
    
        // Iteramos los sprints para mostrar cada uno con su botón de toggle
        for (Sprint sprint : sprints) {
            KeyboardRow row = new KeyboardRow();
            // Se muestra un icono según el estado del sprint
            String statusIcon = "Active".equalsIgnoreCase(sprint.getDescription()) ? "🟢" : "🔴";
            String sprintLabel = statusIcon + " " + sprint.getName() + " (ID: " + sprint.getId() + ") #SPRINT#";
            row.add(sprintLabel);
            // Botón para cambiar estado:
            String toggleButton;
            if ("Active".equalsIgnoreCase(sprint.getDescription())) {
                toggleButton = "Deshabilitar-" + sprint.getId();
            } else {
                toggleButton = "Habilitar-" + sprint.getId();
            }
            row.add(toggleButton);
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
        // Dentro de listTasksForSprint(), antes de mostrar las tareas:
        KeyboardRow reportRow = new KeyboardRow();
        reportRow.add("📊 Reports");
        rows.add(reportRow);
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
    private void showReports(long chatId, BotConversationState state) {
        // 1. Obtener usuarios del proyecto (se puede reutilizar el endpoint que ya existe)
        String urlUsers = baseUrl + "/api/project-users/project/" + state.currentProjectId + "/users";
        ResponseEntity<OracleUser[]> respUsers = restTemplate.getForEntity(urlUsers, OracleUser[].class);
        OracleUser[] users = respUsers.getBody();
    
        StringBuilder reportMsg = new StringBuilder("*Reporte de Tareas Completadas*\n\n");
    
        if (users != null && users.length > 0) {
            for (OracleUser user : users) {
                reportMsg.append("- ").append(user.getName()).append(":\n");
    
                // 2. Consultar las tareas completadas para cada usuario en el sprint actual.
                // Se asume que el endpoint creado a continuación devuelve la lista de TaskAssignees
                String urlCompleted = baseUrl + "/api/task-assignees/user/" + user.getIdUser() + "/sprint/" + state.currentSprintId + "/done";
                ResponseEntity<TaskAssignees[]> respTasks = restTemplate.getForEntity(urlCompleted, TaskAssignees[].class);
                TaskAssignees[] tasks = respTasks.getBody();
    
                int totalCompleted = (tasks != null) ? tasks.length : 0;
                reportMsg.append("      Completed Tasks:   Tot: ").append(totalCompleted).append("\n");
    
                // 3. Para cada tarea, se muestran los detalles.
                if (tasks != null && tasks.length > 0) {
                    for (TaskAssignees ta : tasks) {
                        Tasks task = ta.getTask();
                        // Se asume que en el objeto Tasks existen los métodos getDescription(), getRealHours() y getEstimatedHours()
                        reportMsg.append("         - ").append(task.getDescription())
                                 .append("       Real Hours: ").append(task.getRealHours())
                                 .append("      StimatedHours: ").append(task.getEstimatedHours())
                                 .append("\n");
                    }
                }
                reportMsg.append("\n");
            }
        } else {
            reportMsg.append("No se encontraron usuarios asignados al proyecto.");
        }
        
        sendMsg(chatId, reportMsg.toString(), true);
    }
    
    private void processAddSprintFlow(long chatId, String messageText, BotConversationState state) {
        System.out.println("STATE STEP: " + state.step);
        if (state.step == 1) {
            state.newSprintName = messageText;
            state.step = 2;
            sendMsg(chatId, "Confirma el nuevo sprint con el nombre: *" + state.newSprintName + "*.\n" +
                    "Escribe `/confirmar` para proceder o `/cancel` para cancelar.", true);
        } else if (state.step == 2) {
            if (messageText.equalsIgnoreCase("/confirmar")) {
                Sprint sprint = new Sprint();
                sprint.setName(state.newSprintName);
                sprint.setDescription("Active");
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
                boolean isManager = userRoleServiceBot.isManagerInProject(
                    state.currentProjectId, 
                    state.loggedUser.getIdUser()
                );
                showSprintsForProject(chatId, state.currentProjectId, isManager);
            } else if (messageText.equalsIgnoreCase("/cancel")) {
                sendMsg(chatId, "Creación de sprint cancelada.", false);
                resetFlow(state);
                boolean isManager = userRoleServiceBot.isManagerInProject(
                    state.currentProjectId, 
                    state.loggedUser.getIdUser()
                );
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
