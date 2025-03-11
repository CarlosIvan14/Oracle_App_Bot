package com.springboot.MyTodoList.controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    private ToDoItemService toDoItemService;
    private String botName;
    
    // Mapa para almacenar el estado de conversación por chat (chatId -> estado)
    private Map<Long, BotConversationState> conversationStates = new HashMap<>();
    
    public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService) {
        super(botToken);
        logger.info("Bot Token: " + botToken);
        logger.info("Bot name: " + botName);
        this.toDoItemService = toDoItemService;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            // Si existe un estado conversacional activo, se procesa el flujo
            if (conversationStates.containsKey(chatId)) {
                processConversation(update, chatId);
                return;
            }
            
            if (messageText.equals(BotCommands.START_COMMAND.getCommand()) ||
                messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
                
                SendMessage msg = new SendMessage();
                msg.setChatId(chatId);
                msg.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
                
                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
                List<KeyboardRow> rows = new ArrayList<>();
                
                // Primera fila: lista y agregar
                KeyboardRow row1 = new KeyboardRow();
                row1.add(BotLabels.LIST_ALL_ITEMS.getLabel());
                row1.add(BotLabels.ADD_NEW_ITEM.getLabel());
                rows.add(row1);
                
                // Segunda fila: opciones de pantalla
                KeyboardRow row2 = new KeyboardRow();
                row2.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                row2.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
                rows.add(row2);
                
                keyboard.setKeyboard(rows);
                msg.setReplyMarkup(keyboard);
                
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                
            } else if (messageText.contains(BotLabels.DONE.getLabel())) {
                // Marcar tarea como hecha
                String donePart = messageText.substring(0, messageText.indexOf(BotLabels.DASH.getLabel()));
                try {
                    int id = Integer.valueOf(donePart);
                    ToDoItem item = getToDoItemById(id).getBody();
                    item.setDone(true);
                    updateToDoItem(item, id);
                    BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else if (messageText.contains(BotLabels.UNDO.getLabel())) {
                // Marcar tarea como no hecha
                String undoPart = messageText.substring(0, messageText.indexOf(BotLabels.DASH.getLabel()));
                try {
                    int id = Integer.valueOf(undoPart);
                    ToDoItem item = getToDoItemById(id).getBody();
                    item.setDone(false);
                    updateToDoItem(item, id);
                    BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else if (messageText.contains(BotLabels.DELETE.getLabel())) {
                // Eliminar tarea
                String deletePart = messageText.substring(0, messageText.indexOf(BotLabels.DASH.getLabel()));
                try {
                    int id = Integer.valueOf(deletePart);
                    deleteToDoItem(id);
                    BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DELETED.getMessage(), this);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else if (messageText.equals(BotCommands.HIDE_COMMAND.getCommand()) ||
                       messageText.equals(BotLabels.HIDE_MAIN_SCREEN.getLabel())) {
                BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), this);
            } else if (messageText.equals(BotCommands.TODO_LIST.getCommand()) ||
                       messageText.equals(BotLabels.LIST_ALL_ITEMS.getLabel()) ||
                       messageText.equals(BotLabels.MY_TODO_LIST.getLabel())) {
                List<ToDoItem> allItems = getAllToDoItems();
                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
                List<KeyboardRow> rows = new ArrayList<>();
                
                // Fila superior: volver a pantalla principal
                KeyboardRow topRow = new KeyboardRow();
                topRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                rows.add(topRow);
                
                // Fila para agregar nueva tarea
                KeyboardRow addRow = new KeyboardRow();
                addRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
                rows.add(addRow);
                
                // Título de la lista
                KeyboardRow titleRow = new KeyboardRow();
                titleRow.add(BotLabels.MY_TODO_LIST.getLabel());
                rows.add(titleRow);
                
                // Listar tareas activas
                List<ToDoItem> activeItems = allItems.stream()
                        .filter(item -> !item.isDone())
                        .collect(Collectors.toList());
                for (ToDoItem item : activeItems) {
                    // Mapear prioridad a texto
                    String priorityText = "N/A";
                    if (item.getPriority() != null) {
                        switch (item.getPriority()) {
                            case 1: priorityText = "Alta"; break;
                            case 2: priorityText = "Media"; break;
                            case 3: priorityText = "Baja"; break;
                            default: priorityText = "Desconocida";
                        }
                    }
                    String deadlineText = (item.getDeadline() != null) ? item.getDeadline().toString() : "Sin fecha";
                    String userName = (item.getAssignedUser() != null && item.getAssignedUser().getName() != null)
                        ? item.getAssignedUser().getName() : "Sin usuario";
                    
                    KeyboardRow currentRow = new KeyboardRow();
                    currentRow.add(item.getDescription());
                    currentRow.add("P:" + priorityText);
                    currentRow.add("U:" + userName);
                    currentRow.add(deadlineText);
                    currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
                    rows.add(currentRow);
                }
                
                // Listar tareas completadas con opciones de revertir o eliminar
                List<ToDoItem> doneItems = allItems.stream()
                        .filter(ToDoItem::isDone)
                        .collect(Collectors.toList());
                for (ToDoItem item : doneItems) {
                    String priorityText = "N/A";
                    if (item.getPriority() != null) {
                        switch (item.getPriority()) {
                            case 1: priorityText = "Alta"; break;
                            case 2: priorityText = "Media"; break;
                            case 3: priorityText = "Baja"; break;
                            default: priorityText = "Desconocida";
                        }
                    }
                    String deadlineText = (item.getDeadline() != null) ? item.getDeadline().toString() : "Sin fecha";
                    String userName = (item.getAssignedUser() != null && item.getAssignedUser().getName() != null)
                        ? item.getAssignedUser().getName() : "Sin usuario";
                    
                    KeyboardRow currentRow = new KeyboardRow();
                    currentRow.add(item.getDescription());
                    currentRow.add("P:" + priorityText);
                    currentRow.add("U:" + userName);
                    currentRow.add(deadlineText);
                    currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
                    currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
                    rows.add(currentRow);
                }
                
                // Fila inferior: volver a pantalla principal
                KeyboardRow bottomRow = new KeyboardRow();
                bottomRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                rows.add(bottomRow);
                
                keyboard.setKeyboard(rows);
                SendMessage msg = new SendMessage();
                msg.setChatId(chatId);
                msg.setText(BotLabels.MY_TODO_LIST.getLabel());
                msg.setReplyMarkup(keyboard);
                
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else if (messageText.equals(BotCommands.ADD_ITEM.getCommand()) ||
                       messageText.equals(BotLabels.ADD_NEW_ITEM.getLabel())) {
                // Iniciar el flujo conversacional multi-step para agregar tarea
                BotConversationState state = new BotConversationState();
                state.setStep(1);
                conversationStates.put(chatId, state);
                BotHelper.sendMessageToTelegram(chatId, "Por favor, ingresa la descripción de la tarea:", this);
            } else {
                // Fallback: si no es un comando reconocido ni estamos en flujo conversacional,
                // se informa al usuario que debe usar /start o comandos válidos
                BotHelper.sendMessageToTelegram(
                    chatId,
                    "Comando no reconocido. Por favor, usa /start para comenzar o /todolist para ver las tareas.",
                    this
                );
            }
        }
    }
    
    /**
     * Procesa el estado de la conversación para crear una tarea con los nuevos campos.
     */
    private void processConversation(Update update, long chatId) {
        String messageText = update.getMessage().getText();
        BotConversationState state = conversationStates.get(chatId);
        
        try {
            switch (state.getStep()) {
                case 1:
                    // Paso 1: Recibir descripción
                    state.setDescription(messageText);
                    state.setStep(2);
                    conversationStates.put(chatId, state);
                    BotHelper.sendMessageToTelegram(chatId, "Ingresa la fecha límite (formato yyyy-MM-dd):", this);
                    break;
                case 2:
                    // Paso 2: Recibir deadline
                    try {
                        LocalDate deadline = LocalDate.parse(messageText);
                        state.setDeadline(deadline);
                        state.setStep(3);
                        conversationStates.put(chatId, state);
                        BotHelper.sendMessageToTelegram(chatId, "Ingresa la prioridad (1: alta, 2: media, 3: baja):", this);
                    } catch (Exception e) {
                        BotHelper.sendMessageToTelegram(chatId, "Formato de fecha inválido. Por favor, ingresa la fecha en formato yyyy-MM-dd:", this);
                    }
                    break;
                case 3:
                    // Paso 3: Recibir prioridad
                    try {
                        int priority = Integer.parseInt(messageText);
                        if (priority < 1 || priority > 3) {
                            throw new NumberFormatException();
                        }
                        state.setPriority(priority);
                        state.setStep(4);
                        conversationStates.put(chatId, state);
                        BotHelper.sendMessageToTelegram(chatId, "Ingresa el ID del usuario asignado:", this);
                    } catch (Exception e) {
                        BotHelper.sendMessageToTelegram(chatId, "Prioridad inválida. Ingresa 1, 2 o 3:", this);
                    }
                    break;
                case 4:
                    // Paso 4: Recibir ID del usuario asignado y crear la tarea
                    try {
                        int assignedUserId = Integer.parseInt(messageText);
                        state.setAssignedUserId(assignedUserId);
                        
                        // Crear la tarea con los datos recopilados
                        ToDoItem newItem = new ToDoItem();
                        newItem.setDescription(state.getDescription());
                        newItem.setCreation_ts(OffsetDateTime.now());
                        newItem.setDone(false);
                        newItem.setDeadline(state.getDeadline());
                        newItem.setPriority(state.getPriority());
                        
                        // Crear instancia mínima de OracleUser (usa setId)
                        OracleUser assignedUser = new OracleUser();
                        assignedUser.setIdUser(assignedUserId);
                        newItem.setAssignedUser(assignedUser);
                        
                        toDoItemService.addToDoItem(newItem);
                        BotHelper.sendMessageToTelegram(chatId, "¡Tarea creada exitosamente!", this);
                        conversationStates.remove(chatId);
                    } catch (Exception e) {
                        BotHelper.sendMessageToTelegram(chatId, "ID de usuario inválido. Ingresa un número válido:", this);
                    }
                    break;
                default:
                    conversationStates.remove(chatId);
                    BotHelper.sendMessageToTelegram(chatId, "Se ha cancelado la creación de la tarea.", this);
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            conversationStates.remove(chatId);
            BotHelper.sendMessageToTelegram(chatId, "Error en el flujo de creación. Intenta de nuevo.", this);
        }
    }
    
    @Override
    public String getBotUsername() {
        return botName;
    }
    
    // Métodos "internos" para uso en el bot
    
    public List<ToDoItem> getAllToDoItems() {
        return toDoItemService.findAll();
    }
    
    public ResponseEntity<ToDoItem> getToDoItemById(int id) {
        try {
            ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    public ResponseEntity addToDoItem(ToDoItem todoItem) throws Exception {
        ToDoItem td = toDoItemService.addToDoItem(todoItem);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + td.getID());
        responseHeaders.set("Access-Control-Expose-Headers", "location");
        return ResponseEntity.ok().headers(responseHeaders).build();
    }
    
    public ResponseEntity updateToDoItem(ToDoItem toDoItem, int id) {
        try {
            ToDoItem updatedItem = toDoItemService.updateToDoItem(id, toDoItem);
            return new ResponseEntity<>(updatedItem, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    
    public ResponseEntity<Boolean> deleteToDoItem(int id) {
        try {
            boolean flag = toDoItemService.deleteToDoItem(id);
            return new ResponseEntity<>(flag, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Clase interna para mantener el estado de la conversación.
     */
    private static class BotConversationState {
        private int step; // 1: descripción, 2: deadline, 3: prioridad, 4: usuario asignado
        private String description;
        private LocalDate deadline;
        private int priority;
        private int assignedUserId;
        
        public int getStep() {
            return step;
        }
        public void setStep(int step) {
            this.step = step;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public LocalDate getDeadline() {
            return deadline;
        }
        public void setDeadline(LocalDate deadline) {
            this.deadline = deadline;
        }
        public int getPriority() {
            return priority;
        }
        public void setPriority(int priority) {
            this.priority = priority;
        }
        public int getAssignedUserId() {
            return assignedUserId;
        }
        public void setAssignedUserId(int assignedUserId) {
            this.assignedUserId = assignedUserId;
        }
    }
}
