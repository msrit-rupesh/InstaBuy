package com.example.userservice.service;


import com.example.userservice.model.Address;
import com.example.userservice.model.Profile;
import com.example.userservice.model.User;
import com.example.userservice.model.VendorProfile;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VendorProfileRepository;
import jakarta.transaction.Transactional;
import com.example.userservice.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorProfileService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;

    public VendorProfile createVendorProfile(Long userId, VendorProfile vendorprofile) throws Exception {

        User user = getUser(userId);

        if (user.getRole().getId() != 2) {
            throw new AccessDeniedException("Only USER role can have profile");
        }

        if (vendorProfileRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Profile already exists");
        }

        vendorprofile.setUserId(user.getId());
        return vendorProfileRepository.save(vendorprofile);
    }


    public VendorProfile getProfile(Long userId) throws NotFoundException {
        return vendorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }


    public VendorProfile updateProfile(Long userId, VendorProfile updated) throws NotFoundException {
        return vendorProfileRepository.save(updated);
    }

    public void deleteProfile(Long userId) throws NotFoundException {
        vendorProfileRepository.delete(getProfile(userId));
    }


    private User getUser(Long id) throws NotFoundException{
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
