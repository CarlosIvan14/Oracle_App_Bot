package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.repository.OracleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OracleUserService {

    @Autowired
    private OracleUserRepository oracleUserRepository;

    public List<OracleUser> getAllUsers() {
         return oracleUserRepository.findAll();
    }

    public Optional<OracleUser> getUserById(int id) {
         return oracleUserRepository.findById(id);
    }

    public OracleUser registerUser(OracleUser oracleUser) {
         return oracleUserRepository.save(oracleUser);
    }
}
