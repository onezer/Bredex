package com.bredex.car.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ads")
@Data
@NoArgsConstructor
public class AdDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_name")
    @SequenceGenerator(name = "sequence_name", sequenceName = "db_sequence", initialValue = 100)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, length=200)
    private String description;

    @Column(nullable = false)
    private Long price;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDTO user;

    public AdDTO(AdRegistrationDTO ad, UserDTO userDTO) {
        brand = ad.getBrand();
        model = ad.getModel();
        description = ad.getDescription();
        price = Long.parseLong(ad.getPrice());
        user = userDTO;
    }
}
