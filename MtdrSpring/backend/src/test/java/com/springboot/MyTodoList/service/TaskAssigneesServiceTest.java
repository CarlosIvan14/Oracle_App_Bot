package com.springboot.MyTodoList.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.repository.ProjectUserRepository;
import com.springboot.MyTodoList.repository.TaskAssigneesRepository;
import com.springboot.MyTodoList.repository.TasksRepository;

public class TaskAssigneesServiceTest {

    @Mock
    private TaskAssigneesRepository taskAssigneesRepository;

    @Mock
    private TasksRepository tasksRepository;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @InjectMocks
    private TaskAssigneesService taskAssigneesService;

    private Tasks testTask;
    private ProjectUser testProjectUser;
    private TaskAssignees testAssignment;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testTask = new Tasks();
        testTask.setId(1);
        testTask.setName("Test Task");
        
        testProjectUser = new ProjectUser();
        testProjectUser.setIdProjectUser(1);
        
        testAssignment = new TaskAssignees();
        testAssignment.setIdTaskAssignees(1);
        testAssignment.setTask(testTask);
        testAssignment.setProjectUser(testProjectUser);
    }

    @Test
    public void testAddTaskAssigneeSuccess() {
        when(tasksRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(projectUserRepository.findById(1)).thenReturn(Optional.of(testProjectUser));
        when(taskAssigneesRepository.save(any())).thenReturn(testAssignment);

        TaskAssignees result = taskAssigneesService.addTaskAssignee(testAssignment);
        
        assertNotNull(result);
        assertEquals(result.getIdTaskAssignees(), 1);
        verify(tasksRepository).findById(1);
        verify(projectUserRepository).findById(1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddTaskAssigneeInvalidTask() {
        testAssignment.getTask().setId(999);
        when(tasksRepository.findById(999)).thenReturn(Optional.empty());
        
        taskAssigneesService.addTaskAssignee(testAssignment);
    }

    @Test
    public void testUpdateTaskAssignee() {
        ProjectUser newUser = new ProjectUser();
        newUser.setIdProjectUser(2);
        
        when(taskAssigneesRepository.findById(1)).thenReturn(Optional.of(testAssignment));
        when(projectUserRepository.findById(2)).thenReturn(Optional.of(newUser));
        when(taskAssigneesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskAssignees updated = new TaskAssignees();
        updated.setProjectUser(newUser);
        
        TaskAssignees result = taskAssigneesService.updateTaskAssignee(1, updated);
        
        assertEquals(result.getProjectUser().getIdProjectUser(), 2);
        verify(projectUserRepository).findById(2);
    }

    // @Test
    // public void testGetTaskAssigneesBySprintId() {
    //     when(taskAssigneesRepository.findByTaskSprintId(1))
    //         .thenReturn(Collections.singletonList(testAssignment));
        
    //     List<TaskAssigneeResponseDTO> results = taskAssigneesService.getTaskAssigneesBySprintId(1);
        
    //     assertEquals(results.size(), 1);
    //     TaskAssigneeResponseDTO dto = results.get(0);
    //     assertEquals(dto.getTask().getName(), "Test Task");
    //     assertEquals(dto.getProjectUser().getIdProjectUser(), 1);
    // }

    @Test
    public void testGetStaticRealHours() {
        testTask.setRealHours(5.5);
        when(taskAssigneesRepository.findCompletedTasksByProjectUserAndSprint(1, 1))
            .thenReturn(Collections.singletonList(testAssignment));
        
        Double hours = taskAssigneesService.getStaticRealHoursByUserAndSprint(1, 1);
        
        assertEquals(hours, 5.5);
    }

    @Test
    public void testDateRangeMethods() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 31);
        
        when(taskAssigneesRepository.findCompletedTasksByProjectUserAndDateRange(1, 
            from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
            .thenReturn(Collections.singletonList(testAssignment));
        
        List<TaskAssignees> results = taskAssigneesService
            .getCompletedTasksByUserByDateRange(1, from, to);
        
        assertEquals(results.size(), 1);
    }

    @Test
    public void testDeleteTaskAssignee() {
        when(taskAssigneesRepository.existsById(1)).thenReturn(true);
        
        boolean result = taskAssigneesService.deleteTaskAssignee(1);
        
        assertTrue(result);
        verify(taskAssigneesRepository).deleteById(1);
    }

    @Test
    public void testGetCompletedTasksByUserAndSprint() {
        // Arrange
        int testUserId = 1;
        int testSprintId = 100;
        
        TaskAssignees assignment1 = new TaskAssignees();
        Tasks task1 = new Tasks();
        task1.setStatus("Done");
        assignment1.setTask(task1);
        
        TaskAssignees assignment2 = new TaskAssignees();
        Tasks task2 = new Tasks();
        task2.setStatus("Done");
        assignment2.setTask(task2);

        List<TaskAssignees> expectedAssignments = Arrays.asList(assignment1, assignment2);

        when(taskAssigneesRepository.findCompletedTasksByProjectUserAndSprint(testUserId, testSprintId))
            .thenReturn(expectedAssignments);

        // Act
        List<TaskAssignees> result = taskAssigneesService.getCompletedTasksByUserAndSprint(testUserId, testSprintId);

        // Assert
        assertNotNull(result);
        assertEquals(result.size(), 2);
        verify(taskAssigneesRepository).findCompletedTasksByProjectUserAndSprint(testUserId, testSprintId);
    }

}