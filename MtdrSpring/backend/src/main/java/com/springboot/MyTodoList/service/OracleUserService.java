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
		// Se cifra la contraseña antes de guardar
		String hashedPassword = BCrypt.hashpw(oracleUser.getPassword(), BCrypt.gensalt());
		oracleUser.setPassword(hashedPassword);
		return oracleUserRepository.save(oracleUser);
	}

	// Login de usuario: se busca por nombre y se compara la contraseña
	public Optional<OracleUser> loginUser(String name, String password) {
		Optional<OracleUser> user = oracleUserRepository.findByName(name);
		if (user.isPresent() && BCrypt.checkpw(password, user.get().getPassword())) {
			return user;
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<OracleUser> updateUser(int id, OracleUser userUpdates) {
		return oracleUserRepository.findById(id).map(user -> {
			if (userUpdates.getName() != null) {
				user.setName(userUpdates.getName());
			}
			if (userUpdates.getEmail() != null) {
				user.setEmail(userUpdates.getEmail());
			}
			if (userUpdates.getStatus() != null) {
				user.setStatus(userUpdates.getStatus());
			}
			if (userUpdates.getTelegramId() != null) {
				user.setTelegramId(userUpdates.getTelegramId());
			}
			if (userUpdates.getPhoneNumber() != null) {
				user.setPhoneNumber(userUpdates.getPhoneNumber());
			}
			if (userUpdates.getPassword() != null) {
				String hashedPassword = BCrypt.hashpw(userUpdates.getPassword(), BCrypt.gensalt());
				user.setPassword(hashedPassword);
			}
			return oracleUserRepository.save(user);
		});
	}

}
