package com.bredex.car.repository;

import com.bredex.car.model.AdDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdRepository extends JpaRepository<AdDTO, Long> {
    @Query("SELECT a FROM AdDTO a WHERE " +
            "(:model IS NULL OR a.model LIKE %:model%) AND " +
            "(:brand IS NULL OR a.brand LIKE %:brand%) AND " +
            "(:price IS NULL OR a.price <= :price)")
    List<AdDTO> searchAds(@Param("model") String model,
                           @Param("brand") String brand,
                           @Param("price") Long price);
}
