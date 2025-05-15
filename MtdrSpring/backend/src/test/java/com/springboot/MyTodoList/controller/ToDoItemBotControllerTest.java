package com.springboot.MyTodoList.controller;

import org.mockito.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.springframework.web.client.RestTemplate;
import com.springboot.MyTodoList.model.*;
import com.springboot.MyTodoList.service.*;
import java.time.*;
import java.util.*;

public class ToDoItemBotControllerTest {

    @Mock private TaskServiceBot taskService;
    @Mock private TaskCreationServiceBot taskCreationService;
    @Mock private RestTemplate restTemplate;
    
    @InjectMocks
    private ToDoItemBotController controller;
    
    private ChatState state;
    private final long CHAT_ID = 12345L;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new ToDoItemBotController("token", "botName", "http://localhost", 
            mock(ProjectsServiceBot.class), mock(SprintsServiceBot.class), taskService, 
            taskCreationService, mock(UserRoleServiceBot.class));
            
        state = new ChatState();
        state.loggedUser = new OracleUser(1, "test@test.com", "password");
        state.currentProjectId = 100;
        state.currentSprintId = 200;
        controller.chats.put(CHAT_ID, state);
    }

    // ========== TEST CREACIÓN DE TASK ==========
    @Test
    public void createTask_ShouldSetCorrectParameters() throws Exception {
        // Configurar estado del flujo
        state.flow = ToDoItemBotController.Flow.ADD_TASK;
        state.tName = "Test Task";
        state.tDesc = "Test Description";
        state.tDeadline = LocalDate.now();
        state.tSP = 3;
        state.tEst = 2.5;
        state.mode = "FREE";
        
        // Mockear servicio
        Tasks createdTask = new Tasks();
        createdTask.setId(300);
        when(taskCreationService.createTask(any())).thenReturn(createdTask);

        controller.createTask(CHAT_ID, state);

        verify(taskCreationService).createTask(argThat(task ->
            task.getName().equals("Test Task") &&
            task.getDescription().equals("Test Description") &&
            task.getStoryPoints() == 3 &&
            task.getEstimatedHours() == 2.5
        ));
        
        assertEquals(state.currentTaskId, 300);
    }

    // ========== TEST TAREAS COMPLETADAS EN SPRINT ==========
    @Test
    public void getDoneTasksForSprint_ShouldReturnFilteredResults() {
        // Mockear respuesta del servicio
        TaskAssignees ta1 = createTestTask("COMPLETED");
        TaskAssignees ta2 = createTestTask("IN_PROGRESS");
        when(taskService.getTaskAssigneesBySprint(200))
            .thenReturn(Arrays.asList(ta1, ta2));

        List<TaskAssignees> result = controller.taskService.getTaskAssigneesBySprint(200);
        
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getTask().getStatus(), "COMPLETED");
    }

    // ========== TEST TAREAS COMPLETADAS POR USUARIO ==========
    @Test
    public void getDoneTasksForUserInSprint_ShouldApplyFilters() {
        // Mockear servicios
        when(taskCreationService.getProjectUserId(100, 1)).thenReturn(500);
        when(taskService.getUserTaskAssignments(200, 500))
            .thenReturn(Arrays.asList(
                createTestTask("COMPLETED"),
                createTestTask("COMPLETED"),
                createTestTask("IN_PROGRESS")
            ));

        List<Tasks> result = controller.taskService.getUserTaskAssignments(200, 500)
            .stream()
            .filter(ta -> "COMPLETED".equals(ta.getTask().getStatus()))
            .map(TaskAssignees::getTask)
            .collect(Collectors.toList());
        
        assertEquals(result.size(), 2);
    }

    // ========== UTILIDADES ==========
    private TaskAssignees createTestTask(String status) {
        Tasks task = new Tasks();
        task.setStatus(status);
        task.setRealHours(2.0);
        task.setEstimatedHours(1.5);
        
        TaskAssignees ta = new TaskAssignees();
        ta.setTask(task);
        return ta;
    }

    // Clase interna para simular ChatState
    private static class ChatState extends ToDoItemBotController.ChatState {
        public ChatState() {
            currentProjectId = 100;
            currentSprintId = 200;
            loggedUser = new OracleUser(1, "test@test.com", "password");
        }
    }
}
