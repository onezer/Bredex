package com.bredex.car.service;

import com.bredex.car.model.UserDTO;
import com.bredex.car.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserDTO> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserDTO> getByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public boolean isEmailAlreadyRegistered(String email) {
        Optional<UserDTO> result = userRepository.findByEmail(email.toLowerCase());

        return result.isPresent();
    }

    public boolean isUsernameAlreadyRegistered(String username) {
        Optional<UserDTO> result = userRepository.findByUsername(username);

        return result.isPresent();
    }

    public boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(regex);
    }

    public boolean isValidUsername(String username) {
        String regex = "^[a-zA-Z0-9_-]{1,50}$";
        return username != null && username.matches(regex);
    }

    public boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit;
    }
}
