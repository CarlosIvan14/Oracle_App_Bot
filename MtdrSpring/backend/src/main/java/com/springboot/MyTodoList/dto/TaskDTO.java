package com.springboot.MyTodoList.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class TaskDTO {

	private int id;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime creationTs;

	private String name;

	private String status;

	private String description;

	private Integer storyPoints;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime deadline;

	private Double realHours;

	private Double estimatedHours;

	// private List<AssigneeDTO> assignees;

	// Constructors
	public TaskDTO() {
	}

	public TaskDTO(int id, LocalDateTime creationTs, String name, String status, String description,
			Integer storyPoints, LocalDateTime deadline, Double realHours, Double estimatedHours) {
		this.id = id;
		this.creationTs = creationTs;
		this.name = name;
		this.status = status;
		this.description = description;
		this.storyPoints = storyPoints;
		this.deadline = deadline;
		this.realHours = realHours;
		this.estimatedHours = estimatedHours;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDateTime getCreationTs() {
		return creationTs;
	}

	public void setCreationTs(LocalDateTime creationTs) {
		this.creationTs = creationTs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStoryPoints() {
		return storyPoints;
	}

	public void setStoryPoints(Integer storyPoints) {
		this.storyPoints = storyPoints;
	}

	public LocalDateTime getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDateTime deadline) {
		this.deadline = deadline;
	}

	public Double getRealHours() {
		return realHours;
	}

	public void setRealHours(Double realHours) {
		this.realHours = realHours;
	}

	public Double getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(Double estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

}