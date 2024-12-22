package com.bredex.car.repository;

import com.bredex.car.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDTO, Long> {
    Optional<UserDTO> findByUsername(String username);
    Optional<UserDTO> findByEmail(String email);
}
