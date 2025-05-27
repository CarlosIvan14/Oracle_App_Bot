package com.springboot.MyTodoList;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.service.SprintsServiceBot;
import com.springboot.MyTodoList.service.TaskCreationServiceBot;
import com.springboot.MyTodoList.service.TaskServiceBot;
import com.springboot.MyTodoList.service.UserRoleServiceBot;

import org.mockito.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.springboot.MyTodoList.model.*;

import org.springframework.core.ParameterizedTypeReference;

public class Sprint03Test {

	@Mock
	private RestTemplate restTemplate;

	private TaskServiceBot taskService;

	private UserRoleServiceBot userRoleService;

	private TaskCreationServiceBot service;

	private SprintsServiceBot sprintsService;

	@Test
	public void testCreateTask() { // TEST DEMO 01 FOR BOT.

		MockitoAnnotations.openMocks(this);
		service = new TaskCreationServiceBot(new RestTemplateBuilder(), userRoleService);

		// Inject mock RestTemplate via reflection
		try {
			Field restField = TaskCreationServiceBot.class.getDeclaredField("restTemplate");
			restField.setAccessible(true);
			restField.set(service, restTemplate);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to inject mock RestTemplate", e);
		}

		Tasks task = new Tasks();
		task.setName("Test Task");

		String url = "http://140.84.174.78/api/tasks";
		ResponseEntity<Tasks> response = new ResponseEntity<>(task, HttpStatus.OK);

		when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(Tasks.class))).thenReturn(response);

		Tasks createdTask = service.createTask(task);
		assertNotNull(createdTask);
		assertEquals(createdTask.getName(), "Test Task");
	}

	@Test
	public void testGetSprintsByProjectId() { // TEST DEMO 02 FOR BOT.

		MockitoAnnotations.openMocks(this);
		sprintsService = new SprintsServiceBot(new RestTemplateBuilder());

		// Inject mock RestTemplate via reflection
		try {
			Field restTemplateField = SprintsServiceBot.class.getDeclaredField("restTemplate");
			restTemplateField.setAccessible(true);
			restTemplateField.set(sprintsService, restTemplate);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to inject mock RestTemplate", e);
		}

		int projectId = 42;
		String expectedUrl = "http://140.84.174.78/api/sprints/project/" + projectId;

		List<Sprint> mockSprints = Arrays.asList(new Sprint(), new Sprint());
		ResponseEntity<List<Sprint>> responseEntity = new ResponseEntity<>(mockSprints, HttpStatus.OK);

		when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), isNull(),
				ArgumentMatchers.<ParameterizedTypeReference<List<Sprint>>>any())).thenReturn(responseEntity);

		List<Sprint> result = sprintsService.getSprintsByProjectId(projectId);

		verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), isNull(),
				ArgumentMatchers.<ParameterizedTypeReference<List<Sprint>>>any());
		assertNotNull(result);
		assertEquals(result.size(), 2);
	}

	@Test
	public void testGetUserTaskAssignments() { // TEST DEMO 03 FOR BOT.

		MockitoAnnotations.openMocks(this);
		taskService = new TaskServiceBot(new RestTemplateBuilder());

		// Inject mock RestTemplate via reflection
		try {
			Field restTemplateField = TaskServiceBot.class.getDeclaredField("restTemplate");
			restTemplateField.setAccessible(true);
			restTemplateField.set(taskService, restTemplate);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to inject mock RestTemplate", e);
		}

		int sprintId = 1;
		int projectUserId = 2;
		String expectedUrl = "http://140.84.174.78/api/task-assignees/user/2/sprint/1";

		TaskAssignees[] mockResponse = { new TaskAssignees(), new TaskAssignees() };
		when(restTemplate.getForEntity(eq(expectedUrl), eq(TaskAssignees[].class)))
				.thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

		List<TaskAssignees> result = taskService.getUserTaskAssignments(sprintId, projectUserId);

		assertEquals(result.size(), 2);
		verify(restTemplate).getForEntity(expectedUrl, TaskAssignees[].class);
	}

}