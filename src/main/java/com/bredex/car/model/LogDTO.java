package com.bredex.car.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
public class LogDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDTO user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String type; // login or logout
}
