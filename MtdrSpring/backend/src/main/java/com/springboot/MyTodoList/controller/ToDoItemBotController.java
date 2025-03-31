package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.LoginRequest;
import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bot que gestiona login de usuario, menú según rol (manager/developer),
 * y flujos para listar, crear, editar, borrar ToDoItems, así como
 * crear/editar usuarios.
 */
public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);

    private final ToDoItemService toDoItemService;
    private final String botName;
    private final String baseUrl; // e.g. "http://localhost:8081" para tus endpoints
    private final RestTemplate restTemplate;

    /**
     * Estados/Flows posibles en la conversación.
     */
    private enum Flow {
        NONE,       // Sin flujo activo (menú principal)
        LOGIN,      // Flujo de login
        ADD_TASK,   // Flujo para agregar tarea
        ADD_USER,   // Flujo para agregar usuario
        EDIT_SKILL  // Flujo para editar skill de un usuario
    }

    /**
     * Estructura que guarda el estado de la conversación por chat.
     */
    private static class BotConversationState {
        private Flow flow = Flow.NONE;
        private int step = 0;

        // Info de usuario logueado
        private OracleUser loggedUser;

        // Datos para crear tarea
        private String taskDescription;
        private LocalDate taskDeadline;
        private Integer taskPriority;
        private List<OracleUser> aiSortedUsers; // lista devuelta por la AI
        private int chosenPosition; // índice elegido en la lista
        private int currentProjectId;
        private int currentSprintId;

        // Datos para crear usuario
        private String newUserName;
        private String newUserPassword;
        private String newUserSkill;
        private String newUserTelegramId;
        private String newUserTelegramUsername;

        // Datos para editar skill
        private int editUserId;
        private String editNewSkill;
    }

    // Mapa: chatId -> estado de conversación
    private final Map<Long, BotConversationState> conversationStates = new HashMap<>();

    // Harcodeo de proyectos
    // Proyectos con IDs simples (1,2,3...)
    private static List<Map<String, Object>> PROJECTS = List.of(
        Map.of("id", 1, "name", "Proyecto Oracle Migration"),
        Map.of("id", 2, "name", "Proyecto Billing System"),
        Map.of("id", 3, "name", "Proyecto Web Chatbot"),
        Map.of("id", 4, "name", "Proyecto Mobile Banking")
    );

    // Sprints con IDs en decenas según proyecto
    private static final Map<Integer, List<Map<String, Object>>> SPRINTS_DATA = Map.of(
        1, List.of( // Proyecto 1
            Map.of("id", 11, "name", "Sprint 1", "status", "Activo"),
            Map.of("id", 12, "name", "Sprint 2", "status", "Activo")
        ),
        2, List.of( // Proyecto 2
            Map.of("id", 21, "name", "Sprint Alpha", "status", "Cerrado"),
            Map.of("id", 22, "name", "Sprint Beta", "status", "Activo")
        ),
        3, List.of( // Proyecto 3
            Map.of("id", 31, "name", "Sprint Velocidad", "status", "Activo")
        ),
        4, List.of( // Proyecto 4
            Map.of("id", 41, "name", "Sprint Seguridad", "status", "Planificado")
        )
    );

    // Tareas organizadas por sprint (ID sprint + 2 dígitos)
    private static Map<Integer, List<Map<String, Object>>> SPRINT_TASKS = new HashMap<>();

    // Inicializar tareas de ejemplo
    static {
        // Sprint 11 (Proyecto 1)
        SPRINT_TASKS.put(11, new ArrayList<>(Arrays.asList(
            Map.of("id", 1101, "description", "Migrar base de datos", "status", "ASSIGNED"),
            Map.of("id", 1102, "description", "Configurar servidores Oracle", "status", "IN_PROGRESS"),
            Map.of("id", 1103, "description", "Frontend Dev", "status", "ASSIGNED"),
            Map.of("id", 1104, "description", "Conectar API´s", "status", "COMPLETED")
        )));
        
        // Sprint 12 (Proyecto 1)
        SPRINT_TASKS.put(12, new ArrayList<>(Arrays.asList(
            Map.of("id", 1201, "description", "Optimizar consultas SQL", "status", "COMPLETED"),
            Map.of("id", 1202, "description", "Migrar base de datos", "status", "ASSIGNED"),
            Map.of("id", 1203, "description", "Configurar servidores Oracle", "status", "IN_PROGRESS"),
            Map.of("id", 1204, "description", "Frontend Dev", "status", "ASSIGNED"),
            Map.of("id", 1205, "description", "Conectar API´s", "status", "COMPLETED")
        )));

        // Sprint 21 (Proyecto 2)
        SPRINT_TASKS.put(21, new ArrayList<>(Arrays.asList(
            Map.of("id", 2101, "description", "Diseñar módulo facturación", "status", "ASSIGNED"),
            Map.of("id", 2102, "description", "Implementar API de pagos", "status", "IN_PROGRESS"),
            Map.of("id", 2103, "description", "Crear interfaz de usuario", "status", "COMPLETED"),
            Map.of("id", 2104, "description", "Integrar con sistema de terceros", "status", "ASSIGNED"),
            Map.of("id", 2105, "description", "Realizar pruebas de integración", "status", "COMPLETED")

        )));
    }

    private void startLoginFlow(long chatId, BotConversationState state) {
        state.flow = Flow.LOGIN;
        state.step = 1;
        sendMsg(chatId, "¡Bienvenido! Por favor, ingresa tu *nombre* de usuario:", true);
    }

    private void showSprintsForProject(long chatId, int projectId) {
        List<Map<String, Object>> sprints = SPRINTS_DATA.getOrDefault(projectId, new ArrayList<>());
        
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        // Cabecera con botón de regreso
        KeyboardRow backRow = new KeyboardRow();
        backRow.add("⬅️ Volver a Proyectos");
        rows.add(backRow);

        // Título
        KeyboardRow titleRow = new KeyboardRow();
        titleRow.add("🔄 Tasks del Sprint " + projectId);
        rows.add(titleRow);

        // Lista de sprints
        for (Map<String, Object> sprint : sprints) {
            KeyboardRow row = new KeyboardRow();
            String statusIcon = "Activo".equals(sprint.get("status")) ? "🟢" : "🔴";
            row.add(statusIcon + " " + sprint.get("name") + " (ID: " + sprint.get("id") + ")");
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
            logger.error(e.getMessage(), e);
        }
    }

    private void listTasksForSprint(long chatId, int sprintId) {
        List<Map<String, Object>> tasks = SPRINT_TASKS.getOrDefault(sprintId, new ArrayList<>());
        
        // Filtrar por estado
        List<Map<String, Object>> assigned = tasks.stream()
            .filter(t -> "ASSIGNED".equals(t.get("status")))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> inProgress = tasks.stream()
            .filter(t -> "IN_PROGRESS".equals(t.get("status")))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> completed = tasks.stream()
            .filter(t -> "COMPLETED".equals(t.get("status")))
            .collect(Collectors.toList());
    
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
    
        // Botones de navegación
        KeyboardRow headerRow = new KeyboardRow();
        headerRow.add("⬅️ Volver a Sprints");
        rows.add(headerRow);
    
        // Sección Assigned
        if (!assigned.isEmpty()) {
            rows.add(createTitleRow("==📥 ASIGNADAS 📥=="));
            assigned.forEach(task -> {
                KeyboardRow row = new KeyboardRow();
                row.add(task.get("description") + " [ID: " + task.get("id") + "]");
                row.add("▶️ START-" + task.get("id"));
                rows.add(row);
            });
        }
    
        // Sección In Progress
        if (!inProgress.isEmpty()) {
            rows.add(createTitleRow("==⏳ EN PROGRESO ⏳=="));
            inProgress.forEach(task -> {
                KeyboardRow row = new KeyboardRow();
                row.add(task.get("description") + " [ID: " + task.get("id") + "]");
                row.add("❌ CANCEL-" + task.get("id"));
                row.add("✅ DONE-" + task.get("id"));
                rows.add(row);
            });
        }
    
        // Sección Completed
        if (!completed.isEmpty()) {
            rows.add(createTitleRow("==✅ COMPLETADAS ✅=="));
            completed.forEach(task -> {
                KeyboardRow row = new KeyboardRow();
                row.add(task.get("description") + " [ID: " + task.get("id") + "]");
                row.add("↩️ UNDO-" + task.get("id"));
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

    public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService) {
        super(botToken);
        this.botName = botName;
        this.toDoItemService = toDoItemService;
        this.baseUrl = "http://localhost:8081"; // Ajusta según tu backend
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        
        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        BotConversationState state = conversationStates.computeIfAbsent(chatId, k -> new BotConversationState());
    
        // 1. Manejar comandos especiales
        if (messageText.equalsIgnoreCase("/start")) {
            handleStartCommand(chatId, state);
            return;
        } else if (messageText.equalsIgnoreCase("/menu")) {
            handleMenuCommand(chatId, state);
            return;
        } else if (messageText.equalsIgnoreCase("/logout")) {
            logoutUser(chatId);
            return;
        }
    
        // 2. Manejar navegación entre proyectos y sprints
        if (handleNavigation(chatId, messageText, state)) {
            return;
        }
    
        // 3. Si el usuario no está logueado, forzar login
        if (state.loggedUser == null && state.flow != Flow.LOGIN) {
            startLoginFlow(chatId, state);
            return;
        }
    
        // 4. Manejar flujos activos (login, agregar tarea/usuario)
        if (state.flow != Flow.NONE) {
            processFlow(chatId, messageText, state);
            return;
        }
    
        // 5. Manejar acciones en tareas
        if (handleTaskActions(chatId, messageText, state)) {
            return;
        }
    
        // 6. Manejar selección de proyectos
        if (handleProjectSelection(chatId, messageText, state)) {
            return;
        }
    
        // 7. Opciones del menú principal
        handleMainMenuOptions(chatId, messageText, state);
    }
    
    // Métodos auxiliares nuevos
    private void handleStartCommand(long chatId, BotConversationState state) {
        state.flow = Flow.LOGIN;
        state.step = 1;
        sendMsg(chatId, "¡Bienvenido! Por favor, ingresa tu *nombre* de usuario:", true);
    }
    
    private void handleMenuCommand(long chatId, BotConversationState state) {
        resetFlow(state);
        if (state.loggedUser != null) {
            showMainMenu(chatId, state.loggedUser);
        } else {
            sendMsg(chatId, "No hay sesión activa. Usa /start para loguearte.", false);
        }
    }
    
    private boolean handleNavigation(long chatId, String messageText, BotConversationState state) {
        if (messageText.equals("⬅️ Volver a Proyectos")) {
            showMainMenu(chatId, state.loggedUser);
            return true;
        } else if (messageText.equals("⬅️ Volver a Sprints")) {
            if (state.currentProjectId != 0) {
                showSprintsForProject(chatId, state.currentProjectId);
            } else {
                showMainMenu(chatId, state.loggedUser);
            }
            return true;
        }
        return false;
    }
    
    private boolean handleProjectSelection(long chatId, String messageText, BotConversationState state) {
        Matcher projectMatcher = Pattern.compile(".*\\(ID: (\\d+)\\)").matcher(messageText);
        if (projectMatcher.find()) {
            int projectId = Integer.parseInt(projectMatcher.group(1));
            state.currentProjectId = projectId;
            showSprintsForProject(chatId, projectId);
            return true;
        }
        return false;
    }
    
    private boolean handleTaskActions(long chatId, String messageText, BotConversationState state) {
        String[] actionParts = messageText.split("-");
        if (actionParts.length == 2) {
            String action = actionParts[1];
            if (Arrays.asList("START", "CANCEL", "DONE", "UNDO").contains(action)) {
                try {
                    int taskId = Integer.parseInt(actionParts[0].replaceAll("[^\\d]", ""));
                    int sprintId = taskId / 100; // Obtener ID de sprint de los primeros dígitos
                    
                    List<Map<String, Object>> tasks = SPRINT_TASKS.getOrDefault(sprintId, new ArrayList<>());
                    Optional<Map<String, Object>> task = tasks.stream()
                        .filter(t -> (int) t.get("id") == taskId)
                        .findFirst();
                    
                    if (task.isPresent()) {
                        switch(action) {
                            case "START":
                                task.get().put("status", "IN_PROGRESS");
                                break;
                            case "CANCEL":
                                task.get().put("status", "ASSIGNED");
                                break;
                            case "DONE":
                                task.get().put("status", "COMPLETED");
                                break;
                            case "UNDO":
                                task.get().put("status", "IN_PROGRESS");
                                break;
                        }
                        listTasksForSprint(chatId, sprintId);
                    } else {
                        sendMsg(chatId, "⚠️ Tarea no encontrada", false);
                    }
                } catch (NumberFormatException e) {
                    sendMsg(chatId, "❌ ID de tarea inválido", false);
                }
                return true;
            }
        }
        return false;
    }
    
    private void handleMainMenuOptions(long chatId, String messageText, BotConversationState state) {
        OracleUser user = state.loggedUser;
        boolean isManager = "manager".equalsIgnoreCase(user.getRole());
        
        switch (messageText) {
            case "List Tasks":
                if (state.currentSprintId != 0) {
                    listTasksForSprint(chatId, state.currentSprintId);
                } else {
                    sendMsg(chatId, "Primero selecciona un sprint", false);
                }
                break;
                
            case "🏠 Main Menu":
            case "Show Main Screen":  // Para mantener retrocompatibilidad
                showMainMenu(chatId, state.loggedUser);
                break;
            case "Logout":
            case "Logout 🚪":  // Agregar esta línea
                logoutUser(chatId);
                break;
            case "View Users":
                if (isManager) viewUsers(chatId);
                break;
            case "Add Task":
                if (isManager) startAddTaskFlow(chatId);
                break;
            case "Add User":
                if (isManager) startAddUserFlow(chatId);
                break;
            default:
                // Manejar selección de sprint
                Optional<Map<String, Object>> selectedSprint = SPRINTS_DATA.values().stream()
                    .flatMap(List::stream)
                    .filter(s -> messageText.contains(String.valueOf(s.get("id"))))
                    .findFirst();
                
                if (selectedSprint.isPresent()) {
                    state.currentSprintId = (int) selectedSprint.get().get("id");
                    listTasksForSprint(chatId, state.currentSprintId);
                } else {
                    sendMsg(chatId, "Opción no reconocida. Usa el menú.", false);
                }
                break;
        }
    }

    // --------------------------------------------------------------------------------
    //  Manejo de DONE, UNDO, DELETE
    // --------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------
    //  Handlers de Acciones de Tareas
    // --------------------------------------------------------------------------------

    private void handleStartAction(long chatId, String text) {
        int taskId = extractId(text, "-START");
        int sprintId = taskId / 100;
        
        List<Map<String, Object>> tasks = SPRINT_TASKS.getOrDefault(sprintId, new ArrayList<>());
        tasks.stream()
            .filter(t -> (int) t.get("id") == taskId)
            .findFirst()
            .ifPresent(t -> t.put("status", "IN_PROGRESS"));
        
        sendMsg(chatId, "▶️ Tarea en progreso: " + taskId, false);
        listTasksForSprint(chatId, sprintId);
    }

    private void handleCancelAction(long chatId, String text) {
        int taskId = extractId(text, "-CANCEL");
        int sprintId = taskId / 100;
        
        List<Map<String, Object>> tasks = SPRINT_TASKS.getOrDefault(sprintId, new ArrayList<>());
        tasks.stream()
            .filter(t -> (int) t.get("id") == taskId)
            .findFirst()
            .ifPresent(t -> t.put("status", "ASSIGNED"));
        
        sendMsg(chatId, "⏮️ Tarea devuelta a asignadas: " + taskId, false);
        listTasksForSprint(chatId, sprintId);
    }

    private void handleDoneAction(long chatId, String text) {
        int taskId = extractId(text, "-DONE");
        int sprintId = taskId / 100;
        
        List<Map<String, Object>> tasks = SPRINT_TASKS.getOrDefault(sprintId, new ArrayList<>());
        tasks.stream()
            .filter(t -> (int) t.get("id") == taskId)
            .findFirst()
            .ifPresent(t -> t.put("status", "COMPLETED"));
        
        sendMsg(chatId, "✅ Tarea completada: " + taskId, false);
        listTasksForSprint(chatId, sprintId);
    }

    private void handleUndoAction(long chatId, String text) {
        int taskId = extractId(text, "-UNDO");
        int sprintId = taskId / 100;
        
        List<Map<String, Object>> tasks = SPRINT_TASKS.getOrDefault(sprintId, new ArrayList<>());
        tasks.stream()
            .filter(t -> (int) t.get("id") == taskId)
            .findFirst()
            .ifPresent(t -> t.put("status", "IN_PROGRESS"));
        
        sendMsg(chatId, "↩️ Tarea devuelta a en progreso: " + taskId, false);
        listTasksForSprint(chatId, sprintId);
    }

    private int extractId(String text, String command) {
        try {
            return Integer.parseInt(text.replace(command, "").replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            logger.error("Error extrayendo ID: " + e.getMessage());
            return -1;
        }
    }


    // --------------------------------------------------------------------------------
    //  Procesos de Flujo
    // --------------------------------------------------------------------------------
    private void processFlow(long chatId, String messageText, BotConversationState state) {
        switch (state.flow) {
            case LOGIN:
                processLoginFlow(chatId, messageText, state);
                break;
            case ADD_TASK:
                processAddTaskFlow(chatId, messageText, state);
                break;
            case ADD_USER:
                processAddUserFlow(chatId, messageText, state);
                break;
            case EDIT_SKILL:
                processEditSkillFlow(chatId, messageText, state);
                break;
            default:
                break;
        }
    }

    // --------------------------------------------------------------------------------
    //  FLUJO DE LOGIN
    // --------------------------------------------------------------------------------
    private void processLoginFlow(long chatId, String messageText, BotConversationState state) {
        switch (state.step) {
            case 1:
                state.newUserName = messageText.trim();
                state.step = 2;
                sendMsg(chatId, "Ahora ingresa tu *password*:", true);
                break;
            case 2:
                state.newUserPassword = messageText.trim();
                OracleUser logged = doLogin(state.newUserName, state.newUserPassword);
                if (logged == null) {
                    sendMsg(chatId, "Login fallido. Intenta de nuevo con /start", false);
                    conversationStates.remove(chatId);
                } else {
                    state.loggedUser = logged;
                    state.flow = Flow.NONE;
                    state.step = 0;
                    sendMsg(chatId, "¡Login exitoso! Bienvenido, " + logged.getName(), false);
                    showMainMenu(chatId, logged);
                }
                break;
        }
    }

    // --------------------------------------------------------------------------------
    //  MENÚ PRINCIPAL
    // --------------------------------------------------------------------------------
    private void showMainMenu(long chatId, OracleUser user) {
        if (user == null) {
            sendMsg(chatId, "No tienes sesion activa. Usa /start para loguearte.", false);
            return;
        }

        boolean isManager = "manager".equalsIgnoreCase(user.getRole());
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        // Opción: Listar Tareas
        KeyboardRow row1 = new KeyboardRow();
        row1.add("List Tasks");
        rows.add(row1);

        // Opciones especiales si es manager
        if (isManager) {
            KeyboardRow row2 = new KeyboardRow();
            row2.add("View Users");
            rows.add(row2);
        }

        // Opción: Logout
        KeyboardRow rowLogout = new KeyboardRow();
        rowLogout.add("Logout 🚪");
        rows.add(rowLogout);

        // Opciones de proyectos
        KeyboardRow titleRow = new KeyboardRow();
        titleRow.add("==🚀 Proyectos Activos 🚀==");
        rows.add(titleRow);

        for (Map<String, Object> proj : PROJECTS) {
            KeyboardRow row = new KeyboardRow();
            row.add("📁 " + proj.get("name") + " (ID: " + proj.get("id") + ")");
            rows.add(row);
        }
        
        keyboard.setKeyboard(rows);
        
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Menú principal:");
        msg.setReplyMarkup(keyboard);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------------------
    //  LISTAR TAREAS (Manager -> todas, Developer -> solo suyas)
    // --------------------------------------------------------------------------------
    /* private void listTasksForUser(long chatId, OracleUser user) {
        // Filtrar tareas por estado
        List<Map<String, Object>> assignedTasks = SPRINT_TASKS.stream()
                .filter(t -> "ASSIGNED".equalsIgnoreCase((String) t.get("status")))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> inProgressTasks = SPRINT_TASKS.stream()
                .filter(t -> "IN_PROGRESS".equalsIgnoreCase((String) t.get("status")))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> completedTasks = SPRINT_TASKS.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase((String) t.get("status")))
                .collect(Collectors.toList());
    
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
    
        // Botón para volver al menú principal
        KeyboardRow headerRow = new KeyboardRow();
        headerRow.add("Show Main Screen");
        rows.add(headerRow);
    
        // Sección Assigned
        if (!assignedTasks.isEmpty()) {
            rows.add(createTitleRow("=== ASSIGNED ==="));
            for (Map<String, Object> task : assignedTasks) {
                KeyboardRow row = new KeyboardRow();
                row.add((String) task.get("description"));
                row.add("VIEW +");
                row.add(task.get("id") + "-START");
                rows.add(row);
            }
        }
    
        // Sección In Progress
        if (!inProgressTasks.isEmpty()) {
            rows.add(createTitleRow("=== IN PROGRESS ==="));
            for (Map<String, Object> task : inProgressTasks) {
                KeyboardRow row = new KeyboardRow();
                row.add((String) task.get("description"));
                row.add("VIEW +");
                row.add(task.get("id") + "-CANCEL");
                row.add(task.get("id") + "-DONE");
                rows.add(row);
            }
        }
    
        // Sección Completed
        if (!completedTasks.isEmpty()) {
            rows.add(createTitleRow("=== COMPLETED ==="));
            for (Map<String, Object> task : completedTasks) {
                KeyboardRow row = new KeyboardRow();
                row.add((String) task.get("description"));
                row.add(task.get("id") + "-UNDO");
                rows.add(row);
            }
        }
    
        keyboard.setKeyboard(rows);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Tablero Kanban:");
        msg.setReplyMarkup(keyboard);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }
 */
    // --------------------------------------------------------------------------------
    //  FLUJO: AÑADIR TAREA (manager)
    // --------------------------------------------------------------------------------
    private void startAddTaskFlow(long chatId) {
        BotConversationState state = conversationStates.get(chatId);
        state.flow = Flow.ADD_TASK;
        state.step = 1;
        sendMsg(chatId, "Vamos a crear una nueva tarea.\n1) Ingresa la descripción:", false);
    }

    private void processAddTaskFlow(long chatId, String messageText, BotConversationState state) {
        switch (state.step) {
            case 1:
                state.taskDescription = messageText;
                state.step = 2;
                sendMsg(chatId, "2) Ingresa la fecha límite (YYYY-MM-DD):", false);
                break;
            case 2:
                try {
                    LocalDate date = LocalDate.parse(messageText.trim());
                    state.taskDeadline = date;
                    state.step = 3;
                    sendMsg(chatId, "3) Ingresa la prioridad (1=Alta, 2=Media, 3=Baja):", false);
                } catch (Exception e) {
                    sendMsg(chatId, "Formato de fecha inválido. Intenta de nuevo (YYYY-MM-DD):", false);
                }
                break;
            case 3:
                try {
                    int p = Integer.parseInt(messageText.trim());
                    if (p < 1 || p > 3) {
                        throw new NumberFormatException();
                    }
                    state.taskPriority = p;
                    state.step = 4;
                    sendMsg(chatId, "4) Llamando a la AI para asignar usuarios... un momento.", false);

                    List<OracleUser> sorted = callAiForAssignment(state.taskDescription);
                    state.aiSortedUsers = sorted;
                    if (sorted.isEmpty()) {
                        sendMsg(chatId, "No se encontraron usuarios. Cancelo flujo.", false);
                        resetFlow(state);
                    } else {
                        StringBuilder sb = new StringBuilder("Usuarios ordenados:\n");
                        for (int i = 0; i < sorted.size(); i++) {
                            OracleUser u = sorted.get(i);
                            sb.append((i + 1)).append(") ")
                              .append(u.getName())
                              .append(" (ID: ").append(u.getIdUser()).append(") Skill: ")
                              .append(u.getSkill()).append("\n");
                        }
                        sb.append("\nIngresa el *número* del usuario al que deseas asignar esta tarea:");
                        state.step = 5;
                        sendMsg(chatId, sb.toString(), false);
                    }
                } catch (NumberFormatException e) {
                    sendMsg(chatId, "Prioridad inválida. Ingresa 1, 2 o 3:", false);
                }
                break;
                case 5:
                    try {
                        int pos = Integer.parseInt(messageText.trim());
                        int currentSprintId = state.currentSprintId;
                        
                        // Verificar si el sprint tiene lista de tareas
                        if (!SPRINT_TASKS.containsKey(currentSprintId)) {
                            SPRINT_TASKS.put(currentSprintId, new ArrayList<>());
                        }
                        
                        // Generar nuevo ID
                        int newTaskId = currentSprintId * 100 + SPRINT_TASKS.get(currentSprintId).size() + 1;
                        
                        Map<String, Object> newTask = new HashMap<>();
                        newTask.put("id", newTaskId);
                        newTask.put("description", state.taskDescription);
                        newTask.put("deadline", state.taskDeadline);
                        newTask.put("priority", state.taskPriority);
                        newTask.put("status", "ASSIGNED");
                        
                        SPRINT_TASKS.get(currentSprintId).add(newTask);
                        
                        sendMsg(chatId, "✅ Tarea creada!", false);
                        resetFlow(state);
                        listTasksForSprint(chatId, currentSprintId);
                        
                    } catch (Exception e) {
                        logger.error("Error al crear tarea: " + e.getMessage());
                        sendMsg(chatId, "❌ Error al crear la tarea", false);
                    }
                    break;
            }
    }

    // --------------------------------------------------------------------------------
    //  FLUJO: AÑADIR USUARIO (manager)
    // --------------------------------------------------------------------------------
    private void startAddUserFlow(long chatId) {
        BotConversationState state = conversationStates.get(chatId);
        state.flow = Flow.ADD_USER;
        state.step = 1;
        sendMsg(chatId, "Crearemos un nuevo usuario (role=developer).\n1) Ingresa el *nombre*:", false);
    }

    private void processAddUserFlow(long chatId, String messageText, BotConversationState state) {
        switch (state.step) {
            case 1:
                state.newUserName = messageText.trim();
                state.step = 2;
                sendMsg(chatId, "2) Ingresa la *password*:", false);
                break;
            case 2:
                state.newUserPassword = messageText.trim();
                state.step = 3;
                sendMsg(chatId, "3) Ingresa la *skill*:", false);
                break;
            case 3:
                state.newUserSkill = messageText.trim();
                state.step = 4;
                sendMsg(chatId, "4) Ingresa el *Telegram ID*:", false);
                break;
            case 4:
                state.newUserTelegramId = messageText.trim();
                state.step = 5;
                sendMsg(chatId, "5) Ingresa el *Telegram Username* (sin @):", false);
                break;
            case 5:
                state.newUserTelegramUsername = messageText.trim();
                OracleUser newUser = new OracleUser();
                newUser.setName(state.newUserName);
                newUser.setPassword(state.newUserPassword);
                newUser.setRole("developer");
                newUser.setSkill(state.newUserSkill);
                newUser.setTelegramId(Long.parseLong(state.newUserTelegramId));
                newUser.setTelegramUsername(state.newUserTelegramUsername);

                OracleUser created = doRegisterUser(newUser);
                if (created != null && created.getIdUser() > 0) {
                    sendMsg(chatId, "¡Usuario creado exitosamente! ID=" + created.getIdUser(), false);
                } else {
                    sendMsg(chatId, "Error al crear usuario.", false);
                }
                resetFlow(state);
                showMainMenu(chatId, state.loggedUser);
                break;
        }
    }

    // --------------------------------------------------------------------------------
    //  VER / EDITAR USUARIOS (manager)
    // --------------------------------------------------------------------------------
    private void viewUsers(long chatId) {
        List<OracleUser> list = getAllUsers();
        if (list == null || list.isEmpty()) {
            sendMsg(chatId, "No hay usuarios registrados.", false);
            return;
        }
        
        StringBuilder sb = new StringBuilder("👥 *Usuarios registrados:*\n\n");
        for (OracleUser u : list) {
            sb.append("🆔 ID: ").append(u.getIdUser())
              .append("\n👤 Nombre: ").append(u.getName())
              .append("\n🎭 Rol: ").append(u.getRole())
              .append("\n💡 Skill: ").append(u.getSkill())
              .append("\n\n--------------------\n");
        }
        sb.append("\nℹ️ Para editar un skill usa: /editskill [ID]");
    
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
    
        // Fila de botones
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("➕ Add User");
        firstRow.add("🏠 Main Menu");
        
        rows.add(firstRow);
    
        keyboard.setKeyboard(rows);
    
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(sb.toString());
        msg.setReplyMarkup(keyboard);
        msg.enableMarkdown(true);
        
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // EDICIÓN DE SKILL (manager)
    // ------------------------------------------------------------------
    private void processEditSkillFlow(long chatId, String messageText, BotConversationState state) {
        String newSkill = messageText.trim();
        OracleUser updated = doPatchUserSkill(state.editUserId, newSkill);
        if (updated != null) {
            sendMsg(chatId, "Skill actualizado. Nuevo valor: " + updated.getSkill(), false);
        } else {
            sendMsg(chatId, "Error al actualizar skill del usuario " + state.editUserId, false);
        }
        resetFlow(state);
        showMainMenu(chatId, state.loggedUser);
    }

    // --------------------------------------------------------------------------------
    //  LOGOUT
    // --------------------------------------------------------------------------------
    private void logoutUser(long chatId) {
        BotConversationState state = conversationStates.get(chatId);
        if (state != null) {
            // Resetear todo el estado
            state.loggedUser = null;
            state.currentProjectId = 0;
            state.currentSprintId = 0;
            resetFlow(state);
        }
        conversationStates.remove(chatId);
        
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("🔒 Sesión cerrada exitosamente. Usa /start para ingresar de nuevo.");
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));
    
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error al cerrar sesión: " + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------------------
    //  AUXILIARES DE FLUJO
    // --------------------------------------------------------------------------------
    private void resetFlow(BotConversationState state) {
        state.flow = Flow.NONE;
        state.step = 0;
        state.taskDescription = null;
        state.taskDeadline = null;
        state.taskPriority = null;
        state.aiSortedUsers = null;
        state.chosenPosition = 0;
        state.newUserName = null;
        state.newUserPassword = null;
        state.newUserSkill = null;
        state.newUserTelegramId = null;
        state.newUserTelegramUsername = null;
        state.editUserId = 0;
        state.editNewSkill = null;
    }

    private KeyboardRow createTitleRow(String title) {
        KeyboardRow row = new KeyboardRow();
        row.add(title);
        return row;
    }

    private void sendMsg(long chatId, String text, boolean markdown) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        if (markdown) {
            msg.enableMarkdown(true);
        }
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------------------
    //  LLAMADAS AL BACKEND
    // --------------------------------------------------------------------------------
    private OracleUser doLogin(String name, String password) {
        try {
            String url = baseUrl + "/users/login";
            LoginRequest req = new LoginRequest();
            req.setName(name);
            req.setPassword(password);
            ResponseEntity<OracleUser> resp = restTemplate.postForEntity(url, req, OracleUser.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                return resp.getBody();
            }
        } catch (Exception e) {
            logger.error("Error login: " + e.getMessage(), e);
        }
        return null;
    }

    private List<ToDoItem> getAllToDoItems() {
        try {
            String url = baseUrl + "/todolist";
            ResponseEntity<ToDoItem[]> resp = restTemplate.getForEntity(url, ToDoItem[].class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return Arrays.asList(resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Error getAllToDoItems: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<ToDoItem> getToDoItemsByUser(int userId) {
        try {
            String url = baseUrl + "/todolist/user/" + userId;
            ResponseEntity<ToDoItem[]> resp = restTemplate.getForEntity(url, ToDoItem[].class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return Arrays.asList(resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Error getToDoItemsByUser: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private boolean doCreateItem(ToDoItem item) {
        try {
            String url = baseUrl + "/todolist";
            ResponseEntity<Void> resp = restTemplate.postForEntity(url, item, Void.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Error doCreateItem: " + e.getMessage(), e);
            return false;
        }
    }

    // Llamada a PUT/DELETE en tu service, ajusta si usas PATCH, etc.
    public ResponseEntity<ToDoItem> updateToDoItem(int id, ToDoItem item) {
        try {
            ToDoItem updated = toDoItemService.updateToDoItem(id, item);
            if (updated == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    // EJEMPLO en un controlador normal:
    public ResponseEntity<Boolean> deleteToDoItem(int id) {
        try {
            boolean result = toDoItemService.deleteToDoItem(id);
            if (result) {
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ToDoItem> getToDoItemById(int id) {
        try {
            return toDoItemService.getItemById(id);
        } catch (Exception e) {
            logger.error("Error getToDoItemById: " + e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private OracleUser doRegisterUser(OracleUser newUser) {
        try {
            String url = baseUrl + "/users/register";
            ResponseEntity<OracleUser> resp = restTemplate.postForEntity(url, newUser, OracleUser.class);
            if (resp.getStatusCode() == HttpStatus.CREATED) {
                return resp.getBody();
            }
        } catch (Exception e) {
            logger.error("Error registerUser: " + e.getMessage(), e);
        }
        return null;
    }

    private List<OracleUser> getAllUsers() {
        try {
            String url = baseUrl + "/users";
            ResponseEntity<OracleUser[]> resp = restTemplate.getForEntity(url, OracleUser[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return Arrays.asList(resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Error getAllUsers: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private OracleUser doPatchUserSkill(int userId, String newSkill) {
        try {
            String url = baseUrl + "/users/" + userId;
            OracleUser userUpdates = new OracleUser();
            userUpdates.setSkill(newSkill);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OracleUser> request = new HttpEntity<>(userUpdates, headers);
            ResponseEntity<OracleUser> resp = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    request,
                    OracleUser.class
            );
            if (resp.getStatusCode().is2xxSuccessful()) {
                return resp.getBody();
            }
        } catch (Exception e) {
            logger.error("Error doPatchUserSkill: " + e.getMessage(), e);
        }
        return null;
    }

    private List<OracleUser> callAiForAssignment(String description) {
        try {
            String url = baseUrl + "/assignment/by-ai";
            Map<String, String> payload = new HashMap<>();
            payload.put("description", description);

            ResponseEntity<OracleUser[]> resp = restTemplate.postForEntity(url, payload, OracleUser[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return Arrays.asList(resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Error callAiForAssignment: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private String getPriorityText(Integer p) {
        if (p == null) return "N/A";
        switch (p) {
            case 1: return "Alta";
            case 2: return "Media";
            case 3: return "Baja";
            default: return "Desconocida";
        }
    }
}