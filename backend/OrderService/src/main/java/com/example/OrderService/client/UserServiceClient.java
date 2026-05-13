package com.example.OrderService.client;

import com.example.OrderService.dto.AddressResponseDTO;
import com.example.OrderService.dto.ProductResponseDTO;
import com.example.OrderService.dto.VendorProfileResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "USER-SERVICE",
        url = "http://127.0.0.1:8080"
)
public interface UserServiceClient {

    @GetMapping("/auth/vendor/{id}")
    public VendorProfileResponseDTO getVendorProfile(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization
    );


    @GetMapping("/auth/email/{id}")
    String getUserEmail(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/api/profile/billing-address/{id}")
    AddressResponseDTO getBillingAddressById(@PathVariable Long id);


}