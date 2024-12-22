package com.bredex.car.service;

import com.bredex.car.model.LogDTO;
import com.bredex.car.model.UserDTO;
import com.bredex.car.repository.LogRepository;
import com.bredex.car.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LogService logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsUserLoggedOut_UserHasLogoutLog() {
        String username = "testuser";
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);

        LogDTO logoutLog = new LogDTO();
        logoutLog.setType("logout");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userDTO));
        when(logRepository.findFirstByUserOrderByTimestampDesc(userDTO)).thenReturn(Optional.of(logoutLog));

        boolean isLoggedOut = logService.isUserLoggedOut(username);

        assertTrue(isLoggedOut);
        verify(userRepository, times(1)).findByUsername(username);
        verify(logRepository, times(1)).findFirstByUserOrderByTimestampDesc(userDTO);
    }

    @Test
    void testIsUserLoggedOut_UserHasNoLogs() {
        String username = "testuser";
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userDTO));
        when(logRepository.findFirstByUserOrderByTimestampDesc(userDTO)).thenReturn(Optional.empty());

        boolean isLoggedOut = logService.isUserLoggedOut(username);

        assertTrue(isLoggedOut);  // User with no logs is considered logged out
        verify(userRepository, times(1)).findByUsername(username);
        verify(logRepository, times(1)).findFirstByUserOrderByTimestampDesc(userDTO);
    }

    @Test
    void testIsUserLoggedOut_UserHasNonLogoutLog() {
        String username = "testuser";
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);

        LogDTO loginLog = new LogDTO();
        loginLog.setType("login");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userDTO));
        when(logRepository.findFirstByUserOrderByTimestampDesc(userDTO)).thenReturn(Optional.of(loginLog));

        boolean isLoggedOut = logService.isUserLoggedOut(username);

        assertFalse(isLoggedOut);  // User with a non-logout log is considered not logged out
        verify(userRepository, times(1)).findByUsername(username);
        verify(logRepository, times(1)).findFirstByUserOrderByTimestampDesc(userDTO);
    }

    @Test
    void testIsUserLoggedOut_UserNotFound() {
        String username = "nonexistentuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> logService.isUserLoggedOut(username));
        verify(userRepository, times(1)).findByUsername(username);
        verify(logRepository, never()).findFirstByUserOrderByTimestampDesc(any(UserDTO.class));
    }
}
