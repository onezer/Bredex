package com.bredex.car.model;

import lombok.Value;

@Value
public class DisplayAdDTO {
    Long id;

    String brand;

    String model;

    String description;

    Long price;

    String email;

    public DisplayAdDTO(AdDTO ad) {
        id = ad.getId();
        brand = ad.getBrand();
        model = ad.getModel();
        description = ad.getDescription();
        price = ad.getPrice();
        email = ad.getUser().getEmail();
    }
}
