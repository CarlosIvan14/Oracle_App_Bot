package com.springboot.MyTodoList.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Tarea simplificada sin asignación")
public class SimplifiedTaskDTO {

 	@Schema(description = "ID único de la tarea", example = "101")
    private int id;

    @Schema(description = "Nombre de la tarea", example = "Implementar login")
    private String name;

    @Schema(description = "Estado de la tarea", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Descripción de la tarea", example = "Crear login con JWT")
    private String description;

    @Schema(description = "Puntos de historia asignados", example = "5")
    private Integer storyPoints;

    @Schema(description = "Fecha límite de la tarea", example = "2024-05-20T23:59:00")
    private LocalDateTime deadline;

    @Schema(description = "Horas estimadas para completar la tarea", example = "10.0")
    private Double estimatedHours;

    @Schema(description = "Horas reales utilizadas (por defecto 0)", example = "0.0")
    private Double realHours;

    @Schema(description = "Fecha de creación de la tarea", example = "2024-05-10T08:30:00")
    private LocalDateTime creationTs;


	// Default constructor
	public SimplifiedTaskDTO() {
	}

	// Full constructor
	public SimplifiedTaskDTO(int id, String name, String status, String description, Integer storyPoints,
			LocalDateTime deadline, Double estimatedHours, Double realHours, LocalDateTime creationTs) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.description = description;
		this.storyPoints = storyPoints;
		this.deadline = deadline;
		this.estimatedHours = estimatedHours;
		this.realHours = realHours;
		this.creationTs = creationTs;
	}

	// Constructor with 7 parameters
	public SimplifiedTaskDTO(int id, String name, String status, String description, Integer storyPoints,
			LocalDateTime deadline, Double estimatedHours) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.description = description;
		this.storyPoints = storyPoints;
		this.deadline = deadline;
		this.estimatedHours = estimatedHours;
		// Set default values for missing fields
		this.realHours = 0.0;
		this.creationTs = LocalDateTime.now();
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Double getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(Double estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

	public Double getRealHours() {
		return realHours;
	}

	public void setRealHours(Double realHours) {
		this.realHours = realHours;
	}

	public LocalDateTime getCreationTs() {
		return creationTs;
	}

	public void setCreationTs(LocalDateTime creationTs) {
		this.creationTs = creationTs;
	}

	// toString() method for debugging/logging
	@Override
	public String toString() {
		return "SimplifiedTaskDTO{" + "id=" + id + ", name='" + name + '\'' + ", status='" + status + '\''
				+ ", description='" + description + '\'' + ", storyPoints=" + storyPoints + ", deadline=" + deadline
				+ ", estimatedHours=" + estimatedHours + ", realHours=" + realHours + ", creationTs=" + creationTs
				+ '}';
	}

}