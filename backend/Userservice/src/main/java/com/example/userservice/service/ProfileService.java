package com.example.userservice.service;

import com.example.userservice.model.Address;
import com.example.userservice.model.Profile;
import com.example.userservice.model.User;
import com.example.userservice.repository.AddressRepository;
import com.example.userservice.repository.ProfileRepository;
import com.example.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import com.example.userservice.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public Profile createProfile(Long userId, Profile profile) throws Exception {

        User user = getUser(userId);

        if (user.getRole().getId() != 1) {
            throw new AccessDeniedException("Only USER role can have profile");
        }

        if (profileRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Profile already exists");
        }

        profile.setUser(user);
        return profileRepository.save(profile);
    }


    public Profile getProfile(Long userId) throws NotFoundException {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }


    public Profile updateProfile(Long userId, Profile updated) throws NotFoundException {
        return profileRepository.save(updated);
    }

    public void deleteProfile(Long userId) throws NotFoundException {
        profileRepository.delete(getProfile(userId));
    }

    public Profile setDeliveryAddress(Long userId, Long addressId) throws NotFoundException {
        return updateAddress(userId, addressId, true,false);
    }


    public Profile setBillingAddress(Long userId, Long addressId) throws NotFoundException {
        return updateAddress(userId, addressId, false,true);
    }

    private Profile updateAddress(Long userId, Long addressId, boolean delivery,boolean billing) throws NotFoundException{

        Profile profile = getProfile(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("Address does not belong to user");
        }

        if (delivery) {
            profile.setDeliveryAddressId(addressId);
        }
        if (billing){
            profile.setBillingAddressId(addressId);
        }

        return profileRepository.save(profile);
    }

    private User getUser(Long id) throws NotFoundException{
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}