package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "PROJECTS")
public class Projects {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_PROJECT")
	@JsonProperty("id_project") // Para que el JSON asigne este valor correctamente
	private int idProject;

	@Column(name = "CREATION_TS")
	private LocalDateTime creationTs;

	@Column(name = "DELETED_TS")
	private LocalDateTime deletedTs;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "NAME", nullable = false)
	private String name;

	// Un proyecto tiene muchos ProjectUser.
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference // Este es el lado "padre" de la relación.
	private List<ProjectUser> projectUsers;

	public Projects() {
	}

	public Projects(int idProject, LocalDateTime creationTs, LocalDateTime deletedTs, String description, String name) {
		this.idProject = idProject;
		this.creationTs = creationTs;
		this.deletedTs = deletedTs;
		this.description = description;
		this.name = name;
	}

	public int getIdProject() {
		return idProject;
	}

	public void setIdProject(int idProject) {
		this.idProject = idProject;
	}

	public LocalDateTime getCreationTs() {
		return creationTs;
	}

	public void setCreationTs(LocalDateTime creationTs) {
		this.creationTs = creationTs;
	}

	public LocalDateTime getDeletedTs() {
		return deletedTs;
	}

	public void setDeletedTs(LocalDateTime deletedTs) {
		this.deletedTs = deletedTs;
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

	public List<ProjectUser> getProjectUsers() {
		return projectUsers;
	}

	public void setProjectUsers(List<ProjectUser> projectUsers) {
		this.projectUsers = projectUsers;
	}

	@Override
	public String toString() {
		return "Projects{" + "idProject=" + idProject + ", creation_ts=" + creationTs + ", deleted_ts=" + deletedTs
				+ ", description='" + description + '\'' + ", name='" + name + '\'' + '}';
	}

}
