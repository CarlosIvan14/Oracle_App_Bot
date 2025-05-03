package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "PROJECT_USERS")
public class ProjectUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_PROJECT_USER")
	private int idProjectUser; // Renombrado de id_project_user a idProjectUser

	// Relación ManyToOne con OracleUser (no es bidireccional)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_USER", nullable = false)
	private OracleUser user;

	// Relación ManyToOne con Projects
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PROJECT", nullable = false)
	@JsonBackReference // Evita recursividad en la serialización
	private Projects project;

	@Column(name = "ROLE_USER")
	private String roleUser;

	@Column(name = "STATUS")
	private String status;

	// Constructor por defecto
	public ProjectUser() {
	}

	// Constructor completo
	public ProjectUser(int idProjectUser, OracleUser user, Projects project, String roleUser, String status) {
		this.idProjectUser = idProjectUser;
		this.user = user;
		this.project = project;
		this.roleUser = roleUser;
		this.status = status;
	}

	// Getters y Setters
	public int getIdProjectUser() {
		return idProjectUser;
	}

	public void setIdProjectUser(int idProjectUser) {
		this.idProjectUser = idProjectUser;
	}

	public OracleUser getUser() {
		return user;
	}

	public void setUser(OracleUser user) {
		this.user = user;
	}

	public Projects getProject() {
		return project;
	}

	public void setProject(Projects project) {
		this.project = project;
	}

	public String getRoleUser() {
		return roleUser;
	}

	public void setRoleUser(String roleUser) {
		this.roleUser = roleUser;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		// Evita forzar la carga de project
		return "ProjectUser{" + "idProjectUser=" + idProjectUser + ", project="
				+ (project != null ? project.getIdProject() : null) + ", roleUser='" + roleUser + '\'' + ", status='"
				+ status + '\'' + '}';
	}

}
