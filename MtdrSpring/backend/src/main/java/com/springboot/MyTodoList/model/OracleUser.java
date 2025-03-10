package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "ORACLE_USERS")
public class OracleUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private int idUser;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "ROLE")
    private String role;

    @Column(name = "SKILL")
    private String skill;

    @Column(name = "TELEGRAM_ID")
    private Long telegramId;

    @Column(name = "TELEGRAM_USERNAME")
    private String telegramUsername;

    // Constructors
    public OracleUser() {}

    public OracleUser(int idUser, String name, String password, String role, String skill, Long telegramId, String telegramUsername) {
        this.idUser = idUser;
        this.name = name;
        this.password = password;
        this.role = role;
        this.skill = skill;
        this.telegramId = telegramId;
        this.telegramUsername = telegramUsername;
    }

    // Getters and Setters
    public int getId() {
        return idUser;
    }

    public void setId(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }

    @Override
    public String toString() {
        return "OracleUser{" +
                "idUser=" + idUser +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", skill='" + skill + '\'' +
                ", telegramId=" + telegramId +
                ", telegramUsername='" + telegramUsername + '\'' +
                '}';
    }
}