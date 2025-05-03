package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "SPRINTS")
public class Sprint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_SPRINT")
	private int sprintId;

	@Column(name = "CREATION_TS")
	private LocalDateTime creationTs;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "NAME", nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "ID_PROJECT", nullable = false)
	private Projects project; // Relación con Projects

	@OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<Tasks> tasks;

	public Sprint() {
	}

	public Sprint(int sprintId, LocalDateTime creationTs, String description, String name, Projects project,
			List<Tasks> tasks) {
		this.sprintId = sprintId;
		this.creationTs = creationTs;
		this.description = description;
		this.name = name;
		this.project = project;
		this.tasks = tasks;
	}

	// Agrega la anotación en el getter para forzar que se serialice con la clave
	// "id_sprint"
	@JsonProperty("id_sprint")
	public int getId() {
		return sprintId;
	}

	public void setId(int sprintId) {
		this.sprintId = sprintId;
	}

	public LocalDateTime getCreationTs() {
		return creationTs;
	}

	public void setCreationTs(LocalDateTime creationTs) {
		this.creationTs = creationTs;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Projects getProject() {
		return project;
	}

	public void setProject(Projects project) {
		this.project = project;
	}

	public List<Tasks> getTasks() {
		return tasks;
	}

	public void setTasks(List<Tasks> tasks) {
		this.tasks = tasks;
	}

}
