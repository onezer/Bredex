package com.bredex.car.service;

import com.bredex.car.model.AdDTO;
import com.bredex.car.model.AdRegistrationDTO;
import com.bredex.car.model.SearchDTO;
import com.bredex.car.model.UserDTO;
import com.bredex.car.repository.AdRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class AdService {
    private final AdRepository adRepository;

    @Value("${service.base-url}")
    private String baseURL;

    @Value("${server.port}")
    private String port;

    void setBaseURL(String url) {
        baseURL = url;
    }

    void setPort(String port) {
        this.port = port;
    }

    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    public boolean isBrandValid(String brand) {
        return brand != null && brand.length() <= 20;
    }

    public boolean isModelValid(String model) {
        return model != null && model.length() <= 20;
    }

    public boolean isDescriptionValid(String desc) {
        return desc != null && desc.length() <= 200;
    }

    public boolean isPriceValid(String price) {
        String regex = "^[0-9]{1,10}$";
        return price != null && price.matches(regex);
    }

    public List<String> searchAds(SearchDTO searchDTO) {
        List<AdDTO> ads = adRepository.searchAds(
                searchDTO.getModel(),
                searchDTO.getBrand(),
                searchDTO.getPrice()!=null ? Long.decode(searchDTO.getPrice()) : null);
        String base = "http://" + baseURL + ":" + port + "/ad/";

        List<String> urls = new ArrayList<>();
        for(AdDTO ad : ads) {
            urls.add(base + ad.getId());
        }

        return urls;
    }

    public Optional<AdDTO> getAdById(Long id) {
        return adRepository.findById(id);
    }

    public Long registerAd(AdRegistrationDTO ad, UserDTO userDTO) {
        AdDTO adDTO = new AdDTO(ad, userDTO);

        Long id = adRepository.save(adDTO).getId();
        log.info("User {} registered an ad with id: {}", userDTO.getUsername(), id);

        return id;
    }

    public boolean doesAdExist(Long adId) {
        return getAdById(adId).isPresent();
    }

    public boolean doesAdBelongsToUser(String username, Long adId) {
        return getAdById(adId).get().getUser().getUsername().equals(username);
    }

    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }
}
