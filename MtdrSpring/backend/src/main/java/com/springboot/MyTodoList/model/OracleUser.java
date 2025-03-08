package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "ORACLE_USERS")
public class OracleUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")  // Opcional: si la columna en la BD se llama ID_USER
    private int id;

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

    public OracleUser() {}

    // Getters y Setters
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
}
