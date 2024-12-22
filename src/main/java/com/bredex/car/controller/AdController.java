package com.bredex.car.controller;

import com.bredex.car.model.*;
import com.bredex.car.service.AdService;
import com.bredex.car.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ad")
public class AdController {
    private final AdService adService;
    private final UserService userService;

    public AdController(AdService adService, UserService userService) {
        this.adService = adService;
        this.userService = userService;
    }

    @Operation(summary = "Returns the ad with the given id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAd(@PathVariable(name = "id") Long id) {
        Optional<AdDTO> adOptional = adService.getAdById(id);

        if (adOptional.isPresent()) {
            DisplayAdDTO displayAdDTO = new DisplayAdDTO(adOptional.get());
            return ResponseEntity.ok(displayAdDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Car ad not found with id: " + id);
        }
    }

    @Operation(summary = "Returns a list of links that matches the search terms in the request body")
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchDTO searchDTO) {
        if(searchDTO == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request body!"));
        }

        List<String> list = adService.searchAds(searchDTO);

        if(list.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Posts a used car ad for the user with the given information in the request body")
    @PostMapping
    public ResponseEntity<?> postAd(@RequestBody AdRegistrationDTO adRegistrationDTO) {
        if(adRegistrationDTO == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request body!"));
        }

        List<String> errorMessages = new ArrayList<>();

        if (!adService.isBrandValid(adRegistrationDTO.getBrand())) {
            errorMessages.add("Invalid brand: it must be maximum 20 characters!");
        }
        if (!adService.isDescriptionValid(adRegistrationDTO.getDescription())) {
            errorMessages.add("Invalid description: it must be maximum 200 characters!");
        }
        if (!adService.isModelValid(adRegistrationDTO.getModel())) {
            errorMessages.add("Invalid model: it must be maximum 20 characters!");
        }
        if (!adService.isPriceValid(adRegistrationDTO.getPrice())) {
            errorMessages.add("Invalid price: it must only contain numbers and be maximum 10 digits!");
        }

        if (!errorMessages.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(errorMessages));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<UserDTO> user = userService.getByUsername(username);

        Long id  = adService.registerAd(adRegistrationDTO, user.get());

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("id: " + id));  // we check in token validation if the user is registered

    }

    @Operation(summary = "Deletes the ad with the given id, if it belongs to the user")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable(name = "id") Long id) {
        if(!adService.doesAdExist(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Ad with ID " + id + " not found"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if(!adService.doesAdBelongsToUser(username, id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Unauthorized action"));
        }

        adService.deleteAd(id);

        return ResponseEntity.noContent().build();
    }
}
