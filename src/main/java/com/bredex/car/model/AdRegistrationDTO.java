package com.bredex.car.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdRegistrationDTO {
    private String brand;

    private String model;

    private String description;

    private String price;
}
