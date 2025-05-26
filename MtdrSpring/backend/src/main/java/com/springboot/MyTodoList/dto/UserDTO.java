package com.springboot.MyTodoList.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.springboot.MyTodoList.model.OracleUser;

public class UserDTO {

	private int idUser;

	private String name;

	private String email;

	private String status;

	private Long telegramId;

	private String phoneNumber;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	public UserDTO(OracleUser user) {
		this.idUser = user.getIdUser();
		this.name = user.getName();
		this.email = user.getEmail();
		this.status = user.getStatus();
		this.telegramId = user.getTelegramId();
		this.phoneNumber = user.getPhoneNumber();
	}

	public UserDTO(int idUser, String name, String email, String status, Long telegramId, String phoneNumber) {
		this.idUser = idUser;
		this.name = name;
		this.email = email;
		this.status = status;
		this.telegramId = telegramId;
		this.phoneNumber = phoneNumber;
	}

	public UserDTO() {
	}

	// Getters and Setters
	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getTelegramId() {
		return telegramId;
	}

	public void setTelegramId(Long telegramId) {
		this.telegramId = telegramId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}