package com.springboot.MyTodoList.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.repository.OracleUserRepository;

public class OracleUserServiceTest {

	@Mock
	private OracleUserRepository oracleUserRepository;

	@InjectMocks
	private OracleUserService oracleUserService;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	// -------------------- getAllUsers() --------------------
	@Test
	public void testGetAllUsers() {
		// Arrange
		OracleUser user1 = new OracleUser();
		OracleUser user2 = new OracleUser();
		List<OracleUser> expectedUsers = Arrays.asList(user1, user2);
		when(oracleUserRepository.findAll()).thenReturn(expectedUsers);

		// Act
		List<OracleUser> result = oracleUserService.getAllUsers();

		// Assert
		assertEquals(result, expectedUsers);
		verify(oracleUserRepository).findAll();
	}

	// -------------------- getUserById() --------------------
	@Test
	public void testGetUserByIdFound() {
		// Arrange
		OracleUser user = new OracleUser();
		user.setIdUser(1);
		when(oracleUserRepository.findById(1)).thenReturn(Optional.of(user));

		// Act
		Optional<OracleUser> result = oracleUserService.getUserById(1);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(result.get().getIdUser(), 1);
		verify(oracleUserRepository).findById(1);
	}

	@Test
	public void testGetUserByIdNotFound() {
		// Arrange
		when(oracleUserRepository.findById(999)).thenReturn(Optional.empty());

		// Act
		Optional<OracleUser> result = oracleUserService.getUserById(999);

		// Assert
		assertFalse(result.isPresent());
		verify(oracleUserRepository).findById(999);
	}

	// -------------------- registerUser() --------------------
	@Test
	public void testRegisterUserHashesPassword() {
		// Arrange
		OracleUser inputUser = new OracleUser();
		inputUser.setPassword("plaintext");

		OracleUser savedUser = new OracleUser();
		savedUser.setPassword(BCrypt.hashpw("plaintext", BCrypt.gensalt()));

		when(oracleUserRepository.save(any(OracleUser.class))).thenReturn(savedUser);

		// Act
		OracleUser result = oracleUserService.registerUser(inputUser);

		// Assert
		assertTrue(BCrypt.checkpw("plaintext", result.getPassword()));
		verify(oracleUserRepository).save(any(OracleUser.class));
	}

	// -------------------- loginUser() --------------------
	@Test
	public void testLoginUserSuccess() {
		// Arrange
		String rawPassword = "secret";
		String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

		OracleUser user = new OracleUser();
		user.setName("testuser");
		user.setPassword(hashedPassword);

		when(oracleUserRepository.findByName("testuser")).thenReturn(Optional.of(user));

        // Act
        Optional<UserDTO> result = oracleUserService.loginUser("testuser", rawPassword);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(result.get().getName(), "testuser");
		verify(oracleUserRepository).findByName("testuser");
	}

	@Test
	public void testLoginUserWrongPassword() {
		// Arrange
		OracleUser user = new OracleUser();
		user.setName("testuser");
		user.setPassword(BCrypt.hashpw("correct", BCrypt.gensalt()));

		when(oracleUserRepository.findByName("testuser")).thenReturn(Optional.of(user));

        // Act
        Optional<UserDTO> result = oracleUserService.loginUser("testuser", "wrong");

		// Assert
		assertFalse(result.isPresent());
	}

	// -------------------- updateUser() --------------------
	@Test
	public void testUpdateUserAllFields() {
		// Arrange
		OracleUser existingUser = new OracleUser();
		existingUser.setIdUser(1);
		existingUser.setName("oldName");
		existingUser.setEmail("old@example.com");
		existingUser.setPassword("oldHash");
		existingUser.setStatus("active");
		existingUser.setTelegramId(123L);
		existingUser.setPhoneNumber("555-1234");

        // Create UserDTO with updates instead of OracleUser
        UserDTO updates = new UserDTO();
        updates.setName("newName");
        updates.setEmail("new@example.com");
        updates.setStatus("inactive");
        updates.setTelegramId(456L);
        updates.setPhoneNumber("555-5678");
        updates.setPassword("newPassword"); // This will be hashed

		when(oracleUserRepository.findById(1)).thenReturn(Optional.of(existingUser));
		when(oracleUserRepository.save(any(OracleUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<UserDTO> result = oracleUserService.updateUser(1, updates);

        // Assert
        assertTrue(result.isPresent());
        UserDTO updatedUserDto = result.get();
        
        // Verify DTO fields
        assertEquals("newName", updatedUserDto.getName());
        assertEquals("new@example.com", updatedUserDto.getEmail());
        assertEquals("inactive", updatedUserDto.getStatus());
        assertEquals(Long.valueOf(456L), updatedUserDto.getTelegramId());
        assertEquals("555-5678", updatedUserDto.getPhoneNumber());
        
        // Verify password was hashed by checking the saved entity
        verify(oracleUserRepository).save(argThat(savedUser -> 
            BCrypt.checkpw("newPassword", savedUser.getPassword()) &&
            savedUser.getName().equals("newName") &&
            savedUser.getEmail().equals("new@example.com")
        ));
    }

	@Test
	public void testUpdateUserNotFound() {
		// Arrange
		when(oracleUserRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = oracleUserService.updateUser(999, new UserDTO());

		// Assert
		assertFalse(result.isPresent());
	}

}