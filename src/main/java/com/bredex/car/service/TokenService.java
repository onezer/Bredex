package com.bredex.car.service;

import com.bredex.car.model.TokenDTO;
import com.bredex.car.repository.TokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public boolean tokenExists(String token) {
        Optional<TokenDTO> result = tokenRepository.findByToken(token);

        return result.isPresent();
    }

    public void deleteTokensOfUser(String username) {
        tokenRepository.deleteByUsername(username);
    }

    public void saveToken(String token, String username) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setUsername(username);

        tokenRepository.save(tokenDTO);
    }
}
