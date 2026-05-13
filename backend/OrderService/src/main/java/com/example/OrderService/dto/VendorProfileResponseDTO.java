package com.example.OrderService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorProfileResponseDTO {

    private String companyName;
    private String email;
    private String phone;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
