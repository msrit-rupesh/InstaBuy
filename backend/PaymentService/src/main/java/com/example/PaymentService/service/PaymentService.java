package com.example.PaymentService.service;

import com.example.PaymentService.dto.StripeDTO;
import com.example.PaymentService.model.StripeEvent;
import com.example.PaymentService.repository.StripeEventRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    @Value("${stripe.secretkey}")
    private String secretKey;

    private final StripeEventRepository stripeEventRepository;

    public PaymentService(StripeEventRepository stripeEventRepository){
        this.stripeEventRepository=stripeEventRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }


    public String createCheckoutSession(
            Long orderId,
            List<StripeDTO> items
    ) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (StripeDTO item : items) {
            lineItems.add(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("inr")
                                            .setUnitAmount(item.getPrice())
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .putExtraParam("payment_method_types", List.of("card", "upi"))
                        .addAllLineItem(lineItems)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond())
                        .setSuccessUrl("http://localhost:3000/payment-success?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl("http://localhost:3000/payment-failure")
                        .putMetadata("orderId", orderId.toString())

                        .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
