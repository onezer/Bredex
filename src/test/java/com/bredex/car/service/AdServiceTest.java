package com.bredex.car.service;

import com.bredex.car.model.*;
import com.bredex.car.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private AdService adService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adService.setBaseURL("localhost");
        adService.setPort("9000");
    }

    @Test
    void testIsBrandValid_Valid() {
        assertTrue(adService.isBrandValid("Toyota"));
    }

    @Test
    void testIsBrandValid_Invalid() {
        assertFalse(adService.isBrandValid("A brand name that is way too long for validation"));
    }

    @Test
    void testIsModelValid_Valid() {
        assertTrue(adService.isModelValid("Corolla"));
    }

    @Test
    void testIsModelValid_Invalid() {
        assertFalse(adService.isModelValid("A model name that is way too long"));
    }

    @Test
    void testIsDescriptionValid_Valid() {
        assertTrue(adService.isDescriptionValid("This is a great car!"));
    }

    @Test
    void testIsDescriptionValid_Invalid() {
        String longDescription = "A".repeat(201);
        assertFalse(adService.isDescriptionValid(longDescription));
    }

    @Test
    void testIsPriceValid_Valid() {
        assertTrue(adService.isPriceValid("12345"));
    }

    @Test
    void testIsPriceValid_Invalid() {
        assertFalse(adService.isPriceValid("12A45"));
        assertFalse(adService.isPriceValid("12345678901"));
    }

    @Test
    void testSearchAds_Found() {
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setModel("Model");
        searchDTO.setBrand("Brand");
        searchDTO.setPrice("10000");

        AdDTO ad1 = new AdDTO();
        ad1.setId(1L);
        AdDTO ad2 = new AdDTO();
        ad2.setId(2L);

        when(adRepository.searchAds(eq("Model"), eq("Brand"), eq(10000L)))
                .thenReturn(List.of(ad1, ad2));

        List<String> result = adService.searchAds(searchDTO);

        assertEquals(2, result.size());
        assertEquals("http://localhost:9000/ad/1", result.get(0));
        assertEquals("http://localhost:9000/ad/2", result.get(1));
    }

    @Test
    void testSearchAds_NotFound() {
        SearchDTO searchDTO = new SearchDTO();
        when(adRepository.searchAds(anyString(), anyString(), any())).thenReturn(List.of());

        List<String> result = adService.searchAds(searchDTO);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAdById_Found() {
        Long adId = 1L;
        AdDTO ad = new AdDTO();
        ad.setId(adId);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        Optional<AdDTO> result = adService.getAdById(adId);

        assertTrue(result.isPresent());
        assertEquals(adId, result.get().getId());
    }

    @Test
    void testGetAdById_NotFound() {
        Long adId = 1L;

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        Optional<AdDTO> result = adService.getAdById(adId);

        assertFalse(result.isPresent());
    }

    @Test
    void testRegisterAd() {
        AdRegistrationDTO adRegistrationDTO = new AdRegistrationDTO();
        adRegistrationDTO.setPrice("10000");
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");

        AdDTO adDTO = new AdDTO(adRegistrationDTO, userDTO);
        adDTO.setId(1L);

        when(adRepository.save(any(AdDTO.class))).thenReturn(adDTO);

        Long result = adService.registerAd(adRegistrationDTO, userDTO);

        assertEquals(1L, result);
        verify(adRepository, times(1)).save(any(AdDTO.class));
    }

    @Test
    void testDoesAdExist_True() {
        Long adId = 1L;

        when(adRepository.findById(adId)).thenReturn(Optional.of(new AdDTO()));

        assertTrue(adService.doesAdExist(adId));
    }

    @Test
    void testDoesAdExist_False() {
        Long adId = 1L;

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        assertFalse(adService.doesAdExist(adId));
    }

    @Test
    void testDoesAdBelongsToUser_True() {
        Long adId = 1L;
        String username = "testuser";

        UserDTO user = new UserDTO();
        user.setUsername(username);

        AdDTO ad = new AdDTO();
        ad.setUser(user);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        assertTrue(adService.doesAdBelongsToUser(username, adId));
    }

    @Test
    void testDoesAdBelongsToUser_False() {
        Long adId = 1L;
        String username = "testuser";

        UserDTO user = new UserDTO();
        user.setUsername("otheruser");

        AdDTO ad = new AdDTO();
        ad.setUser(user);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        assertFalse(adService.doesAdBelongsToUser(username, adId));
    }

    @Test
    void testDeleteAd() {
        Long adId = 1L;

        adService.deleteAd(adId);

        verify(adRepository, times(1)).deleteById(adId);
    }
}
