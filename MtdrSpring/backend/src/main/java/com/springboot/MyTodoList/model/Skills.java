package com.springboot.MyTodoList.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SKILLS")
public class Skills {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_SKILL")
	private int idSkills;

	// Relación ManyToOne con OracleUser
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORACLE_USER", nullable = false)
	private OracleUser oracleUser;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "DESCRIPTION", nullable = false)
	private String description;

	public Skills() {
	}

	public Skills(OracleUser oracleUser, String name, String description) {
		this.oracleUser = oracleUser;
		this.name = name;
		this.description = description;
	}

	public int getIdSkills() {
		return idSkills;
	}

	public void setIdSkills(int idSkills) {
		this.idSkills = idSkills;
	}

	public OracleUser getOracleUser() {
		return oracleUser;
	}

	public void setOracleUser(OracleUser oracleUser) {
		this.oracleUser = oracleUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Skills{" + "idSkills=" + idSkills + ", oracleUser=" + oracleUser.getIdUser() + ", name=" + name
				+ ", description=" + description + '}';
	}

}