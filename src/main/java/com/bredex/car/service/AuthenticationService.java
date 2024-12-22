package com.bredex.car.service;

import com.bredex.car.model.LogDTO;
import com.bredex.car.model.UserDTO;
import com.bredex.car.model.UserLoginDTO;
import com.bredex.car.model.UserRegistrationDTO;
import com.bredex.car.repository.LogRepository;
import com.bredex.car.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@Transactional
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final LogRepository logRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, UserService userService, TokenService tokenService, LogRepository logRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.logRepository = logRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public Long registerUser(UserRegistrationDTO userRegistrationDTO) {
        UserDTO user = new UserDTO();
        user.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
        user.setEmail(userRegistrationDTO.getEmail().toLowerCase());
        user.setUsername(userRegistrationDTO.getUsername());

        return userRepository.save(user).getId();
    }

    public UserDTO login(UserLoginDTO user) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        Optional<UserDTO> userDTOOptional = userService.getByUsername(user.getUsername());

        if(userDTOOptional.isPresent()) {
            UserDTO userDTO = userDTOOptional.get();

            LogDTO log = new LogDTO();
            log.setType("login");
            log.setTimestamp(new Date());
            log.setUser(userDTO);

            logRepository.save(log);

            return userDTO;
        }
        else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public void logout(String username) {
        Optional<UserDTO> userDTOOptional = userService.getByUsername(username);

        if(userDTOOptional.isPresent()) {
            UserDTO userDTO = userDTOOptional.get();

            LogDTO log = new LogDTO();
            log.setType("login");
            log.setTimestamp(new Date());
            log.setUser(userDTO);

            logRepository.save(log);

            tokenService.deleteTokensOfUser(username);
        }
    }
}
