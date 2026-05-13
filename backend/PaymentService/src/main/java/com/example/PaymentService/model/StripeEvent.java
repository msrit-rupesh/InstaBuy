package com.example.PaymentService.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(
        name = "Stripe_events"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripeEvent {

    @Id
    private String id;
}
