package com.springboot.MyTodoList.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.repository.TasksRepository;

public class TasksServiceTest {

    @Mock
    private TasksRepository tasksRepository;

    @InjectMocks
    private TasksService tasksService;

    private final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddTask() {
        Tasks newTask = createTestTask();
        when(tasksRepository.save(any(Tasks.class))).thenReturn(newTask);

        Tasks result = tasksService.addTask(newTask);
        
        assertNotNull(result);
        assertEquals(result.getName(), "Test Task");
        verify(tasksRepository).save(newTask);
    }

    @Test
    public void testFindAllTasks() {
        List<Tasks> tasks = Arrays.asList(createTestTask(), createTestTask());
        when(tasksRepository.findAll()).thenReturn(tasks);

        List<Tasks> result = tasksService.findAllTasks();
        
        assertEquals(result.size(), 2);
        verify(tasksRepository).findAll();
    }

    @Test
    public void testGetTaskByIdFound() {
        Tasks task = createTestTask();
        when(tasksRepository.findById(1)).thenReturn(Optional.of(task));

        Optional<Tasks> result = tasksService.getTaskById(1);
        
        assertTrue(result.isPresent());
        assertEquals(result.get().getName(), "Test Task");
    }

    @Test
    public void testGetTaskByIdNotFound() {
        when(tasksRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Tasks> result = tasksService.getTaskById(999);
        
        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateTaskFullUpdate() {
        // Arrange
        Tasks existingTask = createTestTask();
        Tasks updates = createTestTask();
        updates.setName("Updated Task");
        updates.setStatus("In Progress");
        updates.setDescription("New Description");
        updates.setStoryPoints(5);
        updates.setDeadline(LocalDateTime.now().plusSeconds(3600));
        updates.setRealHours(10.5);
        updates.setEstimatedHours(8.0);

        when(tasksRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(tasksRepository.save(any(Tasks.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Tasks result = tasksService.updateTask(1, updates);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "Updated Task");
        assertEquals(result.getStatus(), "In Progress");
        assertEquals(result.getDescription(), "New Description");
        assertEquals(result.getStoryPoints(), Integer.valueOf(5));
        assertNotNull(result.getDeadline());
        assertEquals(result.getRealHours(), 10.5f);
        assertEquals(result.getEstimatedHours(), 8.0f);
    }

    @Test
    public void testUpdateTaskNotFound() {
        when(tasksRepository.findById(999)).thenReturn(Optional.empty());

        Tasks result = tasksService.updateTask(999, new Tasks());
        
        assertNull(result);
    }

    @Test
    public void testPatchTaskPartialUpdate() throws IOException {
        // Arrange
        Tasks existingTask = createTestTask();
        JsonNode updates = mapper.readTree(
            "{\"name\":\"Patched Task\",\"status\":\"Completed\"}"
        );

        when(tasksRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(tasksRepository.save(any(Tasks.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Tasks result = tasksService.patchTask(1, updates);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "Patched Task");
        assertEquals(result.getStatus(), "Completed");
        // Verify unchanged fields remain
        assertEquals(result.getDescription(), "Test Description");
        assertEquals(result.getStoryPoints(), Integer.valueOf(3));
    }

    @Test
    public void testDeleteTaskSuccess() {
        when(tasksRepository.existsById(1)).thenReturn(true);
        
        boolean result = tasksService.deleteTask(1);
        
        assertTrue(result);
        verify(tasksRepository).deleteById(1);
    }

    @Test
    public void testDeleteTaskNotFound() {
        when(tasksRepository.existsById(999)).thenReturn(false);
        
        boolean result = tasksService.deleteTask(999);
        
        assertFalse(result);
    }

    @Test
    public void testGetTasksBySprint() {
        List<Tasks> tasks = Arrays.asList(createTestTask());
        when(tasksRepository.findBySprintId(1)).thenReturn(tasks);

        List<Tasks> result = tasksService.getTasksBySprint(1);
        
        assertEquals(result.size(), 1);
        verify(tasksRepository).findBySprintId(1);
    }

    @Test
    public void testGetUnassignedTasksBySprint() {
        List<Tasks> tasks = Arrays.asList(createTestTask());
        when(tasksRepository.findUnassignedTasksBySprint(1)).thenReturn(tasks);

        List<Tasks> result = tasksService.getUnassignedTasksBySprint(1);
        
        assertEquals(result.size(), 1);
        verify(tasksRepository).findUnassignedTasksBySprint(1);
    }

    private Tasks createTestTask() {
        Tasks task = new Tasks();
        task.setId(1);
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setStatus("To Do");
        task.setStoryPoints(3);
        task.setCreationTs(LocalDateTime.now());
        task.setSprint(null);  // Add actual sprint if needed
        return task;
    }
}