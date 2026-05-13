package com.example.userservice.controller;

import com.example.userservice.Exception.NotFoundException;
import com.example.userservice.dto.*;
import com.example.userservice.model.Address;
import com.example.userservice.model.Profile;
import com.example.userservice.model.User;
import com.example.userservice.model.VendorProfile;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtUtil;
import com.example.userservice.service.AddressService;
import com.example.userservice.service.ProfileService;
import com.example.userservice.service.UserService;
import com.example.userservice.service.VendorProfileService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final VendorProfileService vendorProfileService;
    private final AddressService addressService;
    private final UserService userService;

    public ProfileController(ProfileService profileService,VendorProfileService vendorProfileService,AddressService addressService,UserService userService){
        this.profileService=profileService;
        this.vendorProfileService=vendorProfileService;
        this.addressService=addressService;
        this.userService=userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createProfile(
            @RequestBody @Valid ProfileDTO profileDto,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Profile profile=new Profile();
            profile.setUser(user);
            profile.setFirstName(profileDto.getFirstName());
            if(profileDto.getLastName()!=null){
                profile.setLastName(profileDto.getLastName());
            }
            profile.setPhone(profileDto.getPhone());
            Profile createdProfile= profileService.createProfile(user.getId(), profile);
            return ResponseEntity.ok(createdProfile);
        }catch (AccessDeniedException e){
            return ResponseEntity.status(403).body(e.getMessage());
        }catch (IllegalStateException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/billing-address/{id}")
    public ResponseEntity<?> getBillingAddressById(@PathVariable Long id) {
        try {
            Profile profile = profileService.getProfile(id);
            Address address=addressService.getAddressById(profile.getBillingAddressId());
            return ResponseEntity.ok(address);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Profile profile = profileService.getProfile(user.getId());
            return ResponseEntity.ok(profile);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateProfile(
            @RequestBody @Valid ProfileDTO profileDto,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Profile profile=new Profile();
            profile.setUser(user);
            profile.setFirstName(profileDto.getFirstName());
            if(profileDto.getLastName()!=null){
                profile.setLastName(profileDto.getLastName());
            }
            profile.setPhone(profileDto.getPhone());
            Profile createdProfile= profileService.updateProfile(user.getId(), profile);
            return ResponseEntity.ok(createdProfile);
        }catch (AccessDeniedException e){
            return ResponseEntity.status(403).body(e.getMessage());
        }catch (IllegalStateException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }


    @PostMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> addAddress(
            @RequestBody @Valid AddressDTO addressDTO,
            Authentication authentication
    ) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Address address=new Address();
            address.setFullName(addressDTO.getFullName());
            address.setPhone(addressDTO.getPhone());
            address.setStreetAddress(addressDTO.getStreetAddress());
            address.setCity(addressDTO.getCity());
            address.setState(addressDTO.getState());
            address.setCountry(addressDTO.getCountry());
            address.setPostalCode(addressDTO.getPostalCode());
            address.setAddressType(addressDTO.getAddressType());
            Address savedAddress = addressService.addAddress(user.getId(), address);
            return ResponseEntity.ok(savedAddress);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }


    @PutMapping("/delivery-address/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> setDelivery(
            @PathVariable Long addressId,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Profile profile=profileService.setDeliveryAddress(user.getId(), addressId);
            return ResponseEntity.ok(profile);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/billing-address/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> setBilling(
            @PathVariable Long addressId,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Profile profile=profileService.setBillingAddress(user.getId(), addressId);
            return ResponseEntity.ok(profile);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/address/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long addressId,
            @RequestBody @Valid AddressDTO addressDTO,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            Address address=new Address();
            address.setFullName(addressDTO.getFullName());
            address.setPhone(addressDTO.getPhone());
            address.setStreetAddress(addressDTO.getStreetAddress());
            address.setCity(addressDTO.getCity());
            address.setState(addressDTO.getState());
            address.setCountry(addressDTO.getCountry());
            address.setPostalCode(addressDTO.getPostalCode());
            address.setAddressType(addressDTO.getAddressType());
            Address updatedAddress=addressService.updateAddress(user.getId(),addressId,address);
            return ResponseEntity.ok(updatedAddress);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/address/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            addressService.deleteAddress(user.getId(),addressId);
            return ResponseEntity.ok("Address has been deleted");
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> createVendorProfile(
            @RequestBody @Valid VendorProfileDTO vendorProfileDTO,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            VendorProfile vendorProfile=new VendorProfile();
            vendorProfile.setUserId(user.getId());
            vendorProfile.setCompanyName(vendorProfileDTO.getCompanyName());
            vendorProfile.setPhone(vendorProfileDTO.getPhone());
            vendorProfile.setStreetAddress(vendorProfileDTO.getStreetAddress());
            vendorProfile.setCity(vendorProfileDTO.getCity());
            vendorProfile.setState(vendorProfileDTO.getState());
            vendorProfile.setCountry(vendorProfileDTO.getCountry());
            vendorProfile.setPostalCode(vendorProfileDTO.getPostalCode());
            VendorProfile createdProfile= vendorProfileService.createVendorProfile(user.getId(), vendorProfile);
            return ResponseEntity.ok(createdProfile);
        }catch (AccessDeniedException e){
            return ResponseEntity.status(403).body(e.getMessage());
        }catch (IllegalStateException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasAnyRole('VENDOR')")
    public ResponseEntity<?> getVendorProfile(Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            VendorProfile profile = vendorProfileService.getProfile(user.getId());
            VendorProfileResponseDTO vendorProfileResponseDTO=new VendorProfileResponseDTO();
            vendorProfileResponseDTO.setCompanyName(profile.getCompanyName());
            vendorProfileResponseDTO.setEmail(user.getEmail());
            vendorProfileResponseDTO.setPhone(profile.getPhone());
            vendorProfileResponseDTO.setStreetAddress(profile.getStreetAddress());
            vendorProfileResponseDTO.setCity(profile.getCity());
            vendorProfileResponseDTO.setState(profile.getState());
            vendorProfileResponseDTO.setCountry(profile.getCountry());
            vendorProfileResponseDTO.setPostalCode(profile.getPostalCode());
            return ResponseEntity.ok(vendorProfileResponseDTO);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/vendor/{id}")
    public ResponseEntity<?> getVendorProfile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            VendorProfile profile = vendorProfileService.getProfile(id);
            return ResponseEntity.ok(profile);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateVendorProfile(
            @RequestBody @Valid VendorProfileDTO vendorProfileDTO,
            Authentication authentication) {
        try {
            org.springframework.security.core.userdetails.User authUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user=userService.getUserByUserName(authUser.getUsername());
            VendorProfile vendorProfile=new VendorProfile();
            vendorProfile.setUserId(user.getId());
            vendorProfile.setCompanyName(vendorProfileDTO.getCompanyName());
            vendorProfile.setPhone(vendorProfileDTO.getPhone());
            vendorProfile.setStreetAddress(vendorProfileDTO.getStreetAddress());
            vendorProfile.setCity(vendorProfileDTO.getCity());
            vendorProfile.setState(vendorProfileDTO.getState());
            vendorProfile.setCountry(vendorProfileDTO.getCountry());
            vendorProfile.setPostalCode(vendorProfileDTO.getPostalCode());
            VendorProfile createdProfile= vendorProfileService.updateProfile(user.getId(), vendorProfile);
            return ResponseEntity.ok(createdProfile);
        }catch (AccessDeniedException e){
            return ResponseEntity.status(403).body(e.getMessage());
        }catch (IllegalStateException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

}