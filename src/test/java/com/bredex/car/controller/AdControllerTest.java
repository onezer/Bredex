package com.bredex.car.controller;

import com.bredex.car.model.*;
import com.bredex.car.service.AdService;
import com.bredex.car.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdControllerTest {

    @Mock
    private AdService adService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AdController adController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetAd_Found() {
        Long adId = 1L;
        AdDTO adDTO = new AdDTO();
        UserDTO user = new UserDTO();
        user.setEmail("test@example.com");
        adDTO.setId(adId);
        adDTO.setUser(user);

        when(adService.getAdById(adId)).thenReturn(Optional.of(adDTO));

        ResponseEntity<?> response = adController.getAd(adId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof DisplayAdDTO);
    }

    @Test
    void testGetAd_NotFound() {
        Long adId = 1L;

        when(adService.getAdById(adId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = adController.getAd(adId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Car ad not found with id: " + adId, response.getBody());
    }

    @Test
    void testSearchAds_Found() {
        SearchDTO searchDTO = new SearchDTO();
        List<String> ads = List.of("Ad1", "Ad2");

        when(adService.searchAds(searchDTO)).thenReturn(ads);

        ResponseEntity<?> response = adController.search(searchDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ads, response.getBody());
    }

    @Test
    void testSearchAds_NotFound() {
        SearchDTO searchDTO = new SearchDTO();

        when(adService.searchAds(searchDTO)).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = adController.search(searchDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testPostAd_Valid() {
        AdRegistrationDTO adRegistrationDTO = new AdRegistrationDTO();
        adRegistrationDTO.setBrand("TestBrand");
        adRegistrationDTO.setDescription("TestDescription");
        adRegistrationDTO.setModel("TestModel");
        adRegistrationDTO.setPrice("10000");

        UserDTO user = new UserDTO();
        user.setUsername("testuser");

        when(adService.isBrandValid(anyString())).thenReturn(true);
        when(adService.isDescriptionValid(anyString())).thenReturn(true);
        when(adService.isModelValid(anyString())).thenReturn(true);
        when(adService.isPriceValid(anyString())).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getByUsername("testuser")).thenReturn(Optional.of(user));
        when(adService.registerAd(any(AdRegistrationDTO.class), any(UserDTO.class))).thenReturn(1L);

        ResponseEntity<?> response = adController.postAd(adRegistrationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
    }

    @Test
    void testPostAd_Invalid() {
        AdRegistrationDTO adRegistrationDTO = new AdRegistrationDTO();
        adRegistrationDTO.setBrand("InvalidBrand");

        when(adService.isBrandValid(anyString())).thenReturn(false);

        ResponseEntity<?> response = adController.postAd(adRegistrationDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    void testDeleteAd_Valid() {
        Long adId = 1L;

        when(adService.doesAdExist(adId)).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(adService.doesAdBelongsToUser("testuser", adId)).thenReturn(true);

        ResponseEntity<?> response = adController.deleteAd(adId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adService, times(1)).deleteAd(adId);
    }

    @Test
    void testDeleteAd_NotFound() {
        Long adId = 1L;

        when(adService.doesAdExist(adId)).thenReturn(false);

        ResponseEntity<?> response = adController.deleteAd(adId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    void testDeleteAd_Unauthorized() {
        Long adId = 1L;

        when(adService.doesAdExist(adId)).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(adService.doesAdBelongsToUser("testuser", adId)).thenReturn(false);

        ResponseEntity<?> response = adController.deleteAd(adId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }
}
