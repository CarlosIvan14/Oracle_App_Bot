package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "ORACLE_USERS")
public class OracleUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ORACLE_USER")
    private int id_Oracle_User;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ID_TELEGRAM")
    private Long telegram_Id;

    @Column(name = "PHONE_NUMBER")
    private String phone_Number;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    public OracleUser() {
    }

    public OracleUser(int id_Oracle_User, String name, String email, String status, Long telegram_Id, String phone_Number, String password) {
        this.id_Oracle_User = id_Oracle_User;
        this.name = name;
        this.email = email;
        this.status = status;
        this.telegram_Id = telegram_Id;
        this.phone_Number = phone_Number;
        this.password = password;
    }

    public int getIdUser() {
        return id_Oracle_User;
    }

    public void setIdUser(int idUser) {
        this.id_Oracle_User = idUser;
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
        return telegram_Id;
    }
    
    public void setTelegramId(Long telegram_Id) {
        this.telegram_Id = telegram_Id;
    }

    public String getPhoneNumber() {
        return phone_Number;
    }
    
    public void setPhoneNumber(String phone_Number) {
        this.phone_Number = phone_Number;
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
                "id_Oracle_User=" + id_Oracle_User +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", telegram_Id=" + telegram_Id +
                ", phone_Number='" + phone_Number + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
