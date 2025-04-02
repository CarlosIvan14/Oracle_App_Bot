package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "ORACLE_USERS")
public class OracleUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ORACLE_USER")
    private int idUser;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ID_TELEGRAM")
    private Long telegramId;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    public OracleUser() {
    }

    public OracleUser(int idUser, String name, String email, String status, Long telegramId, String phoneNumber, String password) {
        this.idUser = idUser;
        this.name = name;
        this.email = email;
        this.status = status;
        this.telegramId = telegramId;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

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

    @Override
    public String toString() {
        return "OracleUser{" +
                "idUser=" + idUser +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", telegramId=" + telegramId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
