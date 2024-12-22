package com.bredex.car.repository;

import com.bredex.car.model.LogDTO;
import com.bredex.car.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LogRepository extends JpaRepository<LogDTO, Long> {
    Optional<LogDTO> findFirstByUserOrderByTimestampDesc(UserDTO user);

}
