package com.bredex.car.repository;

import com.bredex.car.model.TokenDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenDTO, Long> {
    Optional<TokenDTO> findByToken(String token);

    void deleteByUsername(String username);
}
