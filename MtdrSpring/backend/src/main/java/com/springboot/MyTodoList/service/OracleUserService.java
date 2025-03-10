package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.repository.OracleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OracleUserService {

    @Autowired
    private OracleUserRepository oracleUserRepository;

    // Register a new user
    public OracleUser registerUser(OracleUser oracleUser) {
        // Add validation or business logic here if needed
        return oracleUserRepository.save(oracleUser);
    }
}
