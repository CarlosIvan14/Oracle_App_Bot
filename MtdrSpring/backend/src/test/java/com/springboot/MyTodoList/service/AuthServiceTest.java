package com.springboot.MyTodoList.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.springboot.MyTodoList.model.LoginRequest;
import com.springboot.MyTodoList.model.OracleUser;

public class AuthServiceTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private RestTemplateBuilder restTemplateBuilder;

	private AuthService authService;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
		authService = new AuthService(restTemplateBuilder);
	}

	@Test
	public void testDoLoginSuccess() {
		// Arrange
		OracleUser user = new OracleUser();
		user.setName("testuser");
		user.setIdUser(1); // Adjust type if needed (int vs Long)

		ResponseEntity<OracleUser> responseEntity = new ResponseEntity<>(user, HttpStatus.OK);

		when(restTemplate.postForEntity(eq("http://140.84.179.223/api/users/login"), any(LoginRequest.class),
				eq(OracleUser.class))).thenReturn(responseEntity);

		// Act
		OracleUser result = authService.doLogin("testuser", "password");

		// Assert
		Assert.assertNotNull(result);
		Assert.assertEquals(result.getName(), "testuser");
		Assert.assertEquals(result.getIdUser(), 1); // Adjust type if needed

		verify(restTemplate).postForEntity(eq("http://140.84.179.223/api/users/login"), any(LoginRequest.class),
				eq(OracleUser.class));
	}

	@Test
	public void testDoLoginFailureStatus() {
		// Arrange
		ResponseEntity<OracleUser> responseEntity = new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

		when(restTemplate.postForEntity(eq("http://140.84.179.223/api/users/login"), any(LoginRequest.class),
				eq(OracleUser.class))).thenReturn(responseEntity);

		// Act
		OracleUser result = authService.doLogin("testuser", "wrongpassword");

		// Assert
		Assert.assertNull(result);

		verify(restTemplate).postForEntity(eq("http://140.84.179.223/api/users/login"), any(LoginRequest.class),
				eq(OracleUser.class));
	}

	@Test
	public void testDoLoginException() {
		// Arrange
		when(restTemplate.postForEntity(eq("http://140.84.179.223/api/users/login"), any(LoginRequest.class),
				eq(OracleUser.class))).thenThrow(new RuntimeException("Connection error"));

		// Act
		OracleUser result = authService.doLogin("testuser", "password");

		// Assert
		Assert.assertNull(result);

		verify(restTemplate).postForEntity(eq("http://140.84.179.223/api/users/login"), any(LoginRequest.class),
				eq(OracleUser.class));
	}

}