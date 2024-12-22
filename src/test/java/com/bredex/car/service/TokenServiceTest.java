package com.bredex.car.service;

import com.bredex.car.model.TokenDTO;
import com.bredex.car.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTokenExists_TokenPresent() {
        String token = "testToken";
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenDTO));

        boolean exists = tokenService.tokenExists(token);

        assertTrue(exists);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    void testTokenExists_TokenNotPresent() {
        String token = "testToken";

        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        boolean exists = tokenService.tokenExists(token);

        assertFalse(exists);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    void testDeleteTokensOfUser() {
        String username = "testUser";

        doNothing().when(tokenRepository).deleteByUsername(username);

        tokenService.deleteTokensOfUser(username);

        verify(tokenRepository, times(1)).deleteByUsername(username);
    }

    @Test
    void testSaveToken() {
        String token = "testToken";
        String username = "testUser";

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setUsername(username);

        when(tokenRepository.save(any(TokenDTO.class))).thenReturn(tokenDTO);

        tokenService.saveToken(token, username);

        verify(tokenRepository, times(1)).save(tokenDTO);
    }
}
