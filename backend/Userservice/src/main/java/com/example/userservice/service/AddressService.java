package com.example.userservice.service;

import com.example.userservice.Exception.NotFoundException;
import com.example.userservice.model.Address;
import com.example.userservice.model.Profile;
import com.example.userservice.repository.AddressRepository;
import com.example.userservice.repository.ProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final ProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    public AddressService(ProfileRepository profileRepository,AddressRepository addressRepository){
        this.profileRepository=profileRepository;
        this.addressRepository=addressRepository;
    }

    public Address addAddress(Long userId, Address address) throws NotFoundException{

        Profile profile = getProfile(userId);
        profile.addAddress(address);
        profileRepository.save(profile);
        address.setProfile(profile);
        return addressRepository.save(address);
    }

    public List<Address> getAddresses(Long userId) throws NotFoundException {
        Profile profile = getProfile(userId);
        return addressRepository.findByProfileId(profile.getId());
    }

    public Address updateAddress(Long userId, Long addressId, Address updated) throws Exception{

        Profile profile = getProfile(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("Address does not belong to user");
        }



        address.setFullName(updated.getFullName());
        address.setPhone(updated.getPhone());
        address.setStreetAddress(updated.getStreetAddress());
        address.setCity(updated.getCity());
        address.setState(updated.getState());
        address.setCountry(updated.getCountry());
        address.setPostalCode(updated.getPostalCode());
        address.setAddressType(updated.getAddressType());
        return addressRepository.save(address);
    }


    public void deleteAddress(Long userId, Long addressId) throws NotFoundException{

        Profile profile = getProfile(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("Address does not belong to user");
        }

        addressRepository.delete(address);
    }

    private Profile getProfile(Long userId) throws NotFoundException{
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }

    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }
}