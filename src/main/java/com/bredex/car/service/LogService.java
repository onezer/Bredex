package com.bredex.car.service;

import com.bredex.car.model.LogDTO;
import com.bredex.car.model.UserDTO;
import com.bredex.car.repository.LogRepository;
import com.bredex.car.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LogService {
    private final LogRepository logRepository;
    private final UserRepository userRepository;

    public LogService(LogRepository logRepository, UserRepository userRepository) {
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    // checks if the last event for a user is a logout one
    public boolean isUserLoggedOut(String username) {
        Optional<UserDTO> user = userRepository.findByUsername(username);
        if(user.isPresent()) {
            Optional<LogDTO> log = logRepository.findFirstByUserOrderByTimestampDesc(user.get());

            if(log.isPresent() && log.get().getType().equals("logout")) {
                return true;
            }
            else {
                return log.isEmpty();  // if the user doesn't have any logs, we will consider him logged out
            }
        }
        else {
            throw new UsernameNotFoundException(username);
        }
    }
}
