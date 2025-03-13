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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        // 0. Revisar si es /menu
        if (messageText.equalsIgnoreCase("/menu")) {
            BotConversationState st = conversationStates.getOrDefault(chatId, new BotConversationState());
            resetFlow(st);
            if (st.loggedUser != null) {
                showMainMenu(chatId, st.loggedUser);
            } else {
                sendMsg(chatId, "No hay sesión activa. Usa /start para loguearte.", false);
            }
            conversationStates.put(chatId, st);
            return;
        }

        // 1. Procesar DONE, UNDO, DELETE si aparece en el texto
        if (messageText.contains("-DONE")) {
            handleDoneAction(chatId, messageText);
            return;
        } else if (messageText.contains("-UNDO")) {
            handleUndoAction(chatId, messageText);
            return;
        } else if (messageText.contains("-DELETE")) {
            handleDeleteAction(chatId, messageText);
            return;
        }

        // 2. Revisar/crear estado si no existe
        conversationStates.putIfAbsent(chatId, new BotConversationState());
        BotConversationState state = conversationStates.get(chatId);

        // 3. Si el usuario no está logueado, forzamos el flujo de login
        if (state.loggedUser == null && state.flow != Flow.LOGIN) {
            state.flow = Flow.LOGIN;
            state.step = 1;
            sendMsg(chatId, "¡Bienvenido! Por favor, ingresa tu *nombre* de usuario:", true);
            return;
        }

        // 4. Si estamos en medio de un flujo (LOGIN, ADD_TASK, ADD_USER, EDIT_SKILL), procesarlo
        if (state.flow != Flow.NONE) {
            processFlow(chatId, messageText, state);
            return;
        }

        // 5. Menú principal (ya logueado, sin flujo)
        OracleUser user = state.loggedUser;
        boolean isManager = user != null && "manager".equalsIgnoreCase(user.getRole());

        switch (messageText) {
            case "/start":
            case "Show Main Screen":
                showMainMenu(chatId, user);
                break;
            case "Logout":
            case "/logout":
                logoutUser(chatId);
                break;
            case "List Tasks":
                listTasksForUser(chatId, user);
                break;
            default:
                if (isManager) {
                    if (messageText.equals("Add Task")) {
                        startAddTaskFlow(chatId);
                    } else if (messageText.equals("Add User")) {
                        startAddUserFlow(chatId);
                    } else if (messageText.equals("View Users")) {
                        viewUsers(chatId);
                    } else {
                        sendMsg(chatId, "Opción no reconocida. Usa el menú.", false);
                    }
                } else {
                    sendMsg(chatId, "Opción no reconocida. Usa el menú.", false);
                }
                break;
        }
    }

    // --------------------------------------------------------------------------------
    //  Manejo de DONE, UNDO, DELETE
    // --------------------------------------------------------------------------------

    private void handleDoneAction(long chatId, String text) {
        // Si hay prefijo "->", quitarlo:
        if (text.startsWith("->")) {
            text = text.substring(2).trim();
        }
        // Extraer ID antes del "-DONE"
        int dashIndex = text.indexOf("-DONE");
        if (dashIndex == -1) {
            sendMsg(chatId, "No se encontró ID para DONE.", false);
            return;
        }
        String idStr = text.substring(0, dashIndex).trim();
        try {
            int id = Integer.parseInt(idStr);
            ToDoItem item = getToDoItemById(id).getBody();
            if (item == null) {
                sendMsg(chatId, "No se encontró la tarea con ID " + id, false);
                return;
            }
            item.setDone(true);
            updateToDoItem(id, item);
            BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);
        } catch (Exception e) {
            logger.error("Error al procesar DONE: " + e.getMessage(), e);
            sendMsg(chatId, "Error al marcar DONE.", false);
        }
    }

    private void handleUndoAction(long chatId, String text) {
        if (text.startsWith("->")) {
            text = text.substring(2).trim();
        }
        int dashIndex = text.indexOf("-UNDO");
        if (dashIndex == -1) {
            sendMsg(chatId, "No se encontró ID para UNDO.", false);
            return;
        }
        String idStr = text.substring(0, dashIndex).trim();
        try {
            int id = Integer.parseInt(idStr);
            ToDoItem item = getToDoItemById(id).getBody();
            if (item == null) {
                sendMsg(chatId, "No se encontró la tarea con ID " + id, false);
                return;
            }
            item.setDone(false);
            updateToDoItem(id, item);
            BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);
        } catch (Exception e) {
            logger.error("Error al procesar UNDO: " + e.getMessage(), e);
            sendMsg(chatId, "Error al marcar UNDO.", false);
        }
    }

    private void handleDeleteAction(long chatId, String text) {
        if (text.startsWith("->")) {
            text = text.substring(2).trim();
        }
        int dashIndex = text.indexOf("-DELETE");
        if (dashIndex == -1) {
            sendMsg(chatId, "No se encontró ID para DELETE.", false);
            return;
        }
        String idStr = text.substring(0, dashIndex).trim();
        try {
            int id = Integer.parseInt(idStr);
            deleteToDoItem(id);
            BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DELETED.getMessage(), this);
        } catch (Exception e) {
            logger.error("Error al procesar DELETE: " + e.getMessage(), e);
            sendMsg(chatId, "Error al eliminar la tarea.", false);
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
            row2.add("Add Task");
            row2.add("Add User");
            row2.add("View Users");
            rows.add(row2);
        }

        // Opción: Logout
        KeyboardRow rowLogout = new KeyboardRow();
        rowLogout.add("Logout");
        rows.add(rowLogout);

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
    private void listTasksForUser(long chatId, OracleUser user) {
        List<ToDoItem> items;
        if ("manager".equalsIgnoreCase(user.getRole())) {
            items = getAllToDoItems();
        } else {
            items = getToDoItemsByUser(user.getIdUser());
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        // Botón para volver al menú principal
        KeyboardRow headerRow = new KeyboardRow();
        headerRow.add("Show Main Screen");
        rows.add(headerRow);

        // Tareas pendientes
        List<ToDoItem> active = items.stream()
                .filter(i -> !i.isDone())
                .collect(Collectors.toList());
        if (!active.isEmpty()) {
            KeyboardRow titleActive = new KeyboardRow();
            titleActive.add("=== PENDIENTES ===");
            rows.add(titleActive);

            for (ToDoItem item : active) {
                KeyboardRow row = new KeyboardRow();
                String priorityText = getPriorityText(item.getPriority());
                String userName = (item.getAssignedUser() != null) ? item.getAssignedUser().getName() : "Sin user";

                // "id-DONE"
                String line = item.getID() + "-DONE";
                row.add(item.getDescription());
                row.add("P:" + priorityText);
                row.add("U:" + userName);
                // Prefijamos "->" por estética, se limpia en handleDoneAction
                row.add("->" + line);
                rows.add(row);
            }
        }

        // Tareas completadas
        List<ToDoItem> doneList = items.stream()
                .filter(ToDoItem::isDone)
                .collect(Collectors.toList());
        if (!doneList.isEmpty()) {
            KeyboardRow titleDone = new KeyboardRow();
            titleDone.add("=== COMPLETADAS ===");
            rows.add(titleDone);

            for (ToDoItem item : doneList) {
                KeyboardRow row = new KeyboardRow();
                String priorityText = getPriorityText(item.getPriority());
                String userName = (item.getAssignedUser() != null) ? item.getAssignedUser().getName() : "Sin user";

                // "id-UNDO", "id-DELETE"
                String lineUndo = item.getID() + "-UNDO";
                String lineDel = item.getID() + "-DELETE";

                row.add(item.getDescription());
                row.add("P:" + priorityText);
                row.add("U:" + userName);
                row.add("->" + lineUndo);
                row.add("->" + lineDel);
                rows.add(row);
            }
        }

        keyboard.setKeyboard(rows);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Tus tareas:");
        msg.setReplyMarkup(keyboard);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

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
                    if (pos < 1 || pos > state.aiSortedUsers.size()) {
                        sendMsg(chatId, "Posición fuera de rango. Debe ser entre 1 y " + state.aiSortedUsers.size(), false);
                        return;
                    }
                    state.chosenPosition = pos - 1;
                    OracleUser chosenUser = state.aiSortedUsers.get(state.chosenPosition);
                    ToDoItem newItem = new ToDoItem();
                    newItem.setDescription(state.taskDescription);
                    newItem.setCreation_ts(OffsetDateTime.now());
                    newItem.setDeadline(state.taskDeadline);
                    newItem.setPriority(state.taskPriority);
                    newItem.setDone(false);

                    OracleUser assignedUser = new OracleUser();
                    assignedUser.setIdUser(chosenUser.getIdUser());
                    newItem.setAssignedUser(assignedUser);

                    boolean createdOk = doCreateItem(newItem);
                    if (createdOk) {
                        sendMsg(chatId, "¡Tarea creada exitosamente!", false);
                    } else {
                        sendMsg(chatId, "Hubo un error al crear la tarea.", false);
                    }
                    resetFlow(state);
                    showMainMenu(chatId, state.loggedUser);
                } catch (Exception e) {
                    sendMsg(chatId, "Número inválido. Ingresa un valor entre 1 y " + state.aiSortedUsers.size(), false);
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
        StringBuilder sb = new StringBuilder("Usuarios:\n");
        for (OracleUser u : list) {
            sb.append("ID: ").append(u.getIdUser())
              .append(" | ").append(u.getName())
              .append(" | Role: ").append(u.getRole())
              .append(" | Skill: ").append(u.getSkill())
              .append("\n");
        }
        sb.append("\nSi deseas editar el skill de un usuario, escribe: /editskill <id>\n");
        sb.append("O escribe /menu para volver al menú.");
        sendMsg(chatId, sb.toString(), false);
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
        conversationStates.remove(chatId);
        sendMsg(chatId, "Has cerrado sesión. Usa /start para iniciar de nuevo.", false);
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
