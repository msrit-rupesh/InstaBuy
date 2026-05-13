package com.example.OrderService.service;

import com.example.OrderService.client.UserServiceClient;
import com.example.OrderService.dto.AddressResponseDTO;
import com.example.OrderService.dto.VendorProfileResponseDTO;
import com.example.OrderService.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
public class UserService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwt;

    public UserService(UserServiceClient userServiceClient, JwtUtil jwt){
        this.userServiceClient=userServiceClient;
        this.jwt=jwt;
    }

    public VendorProfileResponseDTO getVendorProfile(Long id) {
        String authToken= jwt.generateToken("ORDER-SERVICE","ORDER-SERVICE",0);
        return userServiceClient.getVendorProfile(id,authToken);
    }
    public String getUserEmail(Long id){
        String authToken = "Bearer " + jwt.generateToken("ORDER-SERVICE", "ORDER-SERVICE", 0);
        return userServiceClient.getUserEmail(id,authToken);
    }
    public AddressResponseDTO getBillingAddressById(Long id){
        return userServiceClient.getBillingAddressById(id);
    }

}
