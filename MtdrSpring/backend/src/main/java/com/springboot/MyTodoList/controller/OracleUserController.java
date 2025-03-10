package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.OracleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class OracleUserController {

    @Autowired
    private OracleUserService oracleUserService;

    // POST endpoint to register a new user
    @PostMapping("/register")
    public ResponseEntity<OracleUser> registerUser(@RequestBody OracleUser oracleUser) {
        try {
            OracleUser registeredUser = oracleUserService.registerUser(oracleUser);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
