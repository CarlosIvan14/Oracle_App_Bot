package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.repository.OracleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
        String hashedPassword = BCrypt.hashpw(oracleUser.getPassword(), BCrypt.gensalt());
        oracleUser.setPassword(hashedPassword);
        return oracleUserRepository.save(oracleUser);
    }

    // Login a user
public Optional<OracleUser> loginUser(String name, String password) {
    // Fetch the user by name
    Optional<OracleUser> user = oracleUserRepository.findByName(name);
    
    // Check if the user exists and the password matches
    if (user.isPresent() && BCrypt.checkpw(password, user.get().getPassword())) {
        return user; // Return the user if credentials are valid
    } else {
        return Optional.empty(); // Return empty if credentials are invalid
    }
}

    public Optional<OracleUser> updateUser(int id, OracleUser userUpdates) {
        return oracleUserRepository.findById(id).map(user -> {
            if (userUpdates.getName() != null) {
                user.setName(userUpdates.getName());
            }
            if (userUpdates.getPassword() != null) {
                String hashedPassword = BCrypt.hashpw(userUpdates.getPassword(), BCrypt.gensalt());
                user.setPassword(hashedPassword);
            }
            if (userUpdates.getRole() != null) {
                user.setRole(userUpdates.getRole());
            }
            if (userUpdates.getSkill() != null) {
                user.setSkill(userUpdates.getSkill());
            }
            if (userUpdates.getTelegramUsername() != null) {
                user.setTelegramUsername(userUpdates.getTelegramUsername());
            }
            return oracleUserRepository.save(user);
        });
    }
}
