package com.bredex.car.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchDTO {
    private String model;
    private String brand;
    private String price;
}
