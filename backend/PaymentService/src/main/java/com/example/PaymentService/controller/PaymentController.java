package com.example.PaymentService.controller;

import com.example.PaymentService.dto.*;
import com.example.PaymentService.security.JwtUtil;
import com.example.PaymentService.service.OrderService;
import com.example.PaymentService.service.PaymentEventProducer;
import com.example.PaymentService.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.secretkey}")
    private String webhookSecret;

    private final PaymentEventProducer paymentProducer;

    private final OrderService orderService;
    private final JwtUtil jwtUtil;
    private final PaymentService paymentService;

    public PaymentController(OrderService orderService,JwtUtil jwtUtil, PaymentService paymentService,PaymentEventProducer paymentProducer) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
        this.paymentService = paymentService;
        this.paymentProducer=paymentProducer;
    }

    @PostMapping("/create-checkout-session")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createCheckoutSession(@Valid @RequestBody CheckOutDTO data, Authentication authentication) {
        try {
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            Long userId=authUser.getId();
            Long orderId=data.getOrderId();
            String token=jwtUtil.generateToken("PAYMENT-SERVICE","PAYMENT-SERVICE",0);
            String authToken="Bearer "+token;
            OrderResponse orderResponse=orderService.getOrderById(orderId,userId,authToken);
            log.info("OrderResponse: {}", orderResponse);;
            if(!Objects.equals(orderResponse.getStatus(), "PAYMENT_INITIATED")){
                return ResponseEntity.badRequest().body("Order has not been initialized");
            }
            List<OrderItemResponse> itemsData=orderResponse.getOrderItems();
            List<StripeDTO> items = itemsData.stream().map(item -> {
                StripeDTO dto = new StripeDTO();
                dto.setName(item.getProductId());
                double discountedPrice=item.getPrice()*(100-item.getDiscount())/100;
                dto.setPrice(BigDecimal.valueOf(discountedPrice).multiply(BigDecimal.valueOf(100)).longValue());
                dto.setQuantity(item.getQuantity());
                return dto;
            }).toList();


            String checkoutUrl = paymentService.createCheckoutSession(orderId, items);


            Map<String, String> response = new HashMap<>();
            response.put("url", checkoutUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/payment/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> confirmPayment(
            @RequestBody Map<String , String > body,
            Authentication authentication
    ){
        try{
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            String sessionId=body.get("sessionId");
            Long userId=authUser.getId();

            if (sessionId == null) {
                return ResponseEntity.badRequest().body("Missing sessionId");
            }

            Session session = Session.retrieve(sessionId);

            String orderIdStr = session.getMetadata().get("orderId");
            if (orderIdStr == null) {
                return ResponseEntity.badRequest().body("Order id missing");
            }
            Long orderId = Long.parseLong(orderIdStr);

            PaymentIntent paymentIntent =
                    PaymentIntent.retrieve(session.getPaymentIntent());

            if ("succeeded".equals(paymentIntent.getStatus())) {

                paymentProducer.sendPaymentConfirmed(orderId);
                return ResponseEntity.ok("Payment confirmed");
            }

            paymentProducer.sendPaymentFailed(orderId);
            return ResponseEntity.ok("Order marked failed");

        }
        catch(com.stripe.exception.StripeException e){
            return ResponseEntity.status(409).body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/payment/fail")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> markFailed(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDTO orderDTO,
            Authentication authentication
    ) {
        try{
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            Long userId= authUser.getId();
            String token = "Bearer " +
                    jwtUtil.generateToken("PAYMENT-SERVICE", "PAYMENT-SERVICE", 0);
            Object order =orderService.getOrderById(id,userId,token);
            paymentProducer.sendPaymentFailed(id);
            return ResponseEntity.ok("Order marked failed");
        }catch (NotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }

    }


//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(
//            @RequestBody String payload,
//            @RequestHeader("Stripe-Signature") String sigHeader
//    ) {
//
//        Event event;
//
//        try {
//            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
//        } catch (Exception ex) {
//            return ResponseEntity.badRequest().body("Invalid signature");
//        }
//        String eventId=event.getId();
//        if(paymentService.isEventProcessed(eventId)){
//            return ResponseEntity.badRequest().body("Already processed");
//        }
//
//        try {
//            if ("checkout.session.completed".equals(event.getType())) {
//                Session session =
//                        (Session) event.getDataObjectDeserializer()
//                                .getObject().orElseThrow();
//
//                String orderIdStr = session.getMetadata().get("orderId");
//                if (orderIdStr != null) {
//                    Long orderId = Long.parseLong(orderIdStr);
//                    String token = "Bearer " + jwtUtil.generateToken("PAYMENT-SERVICE", "PAYMENT-SERVICE", 0);
//                    orderService.placeOrder(orderId,userId, token);
//                }
//            }
//
//            if ("checkout.session.expired".equals(event.getType())) {
//                Session session =
//                        (Session) event.getDataObjectDeserializer()
//                                .getObject().orElseThrow();
//
//                String orderIdStr = session.getMetadata().get("orderId");
//                if (orderIdStr != null) {
//                    Long orderId = Long.parseLong(orderIdStr);
//                    String token = "Bearer " + jwtUtil.generateToken("PAYMENT-SERVICE", "PAYMENT-SERVICE", 0);
//                    orderService.placeOrderFailed(orderId, token);
//                }
//            }
//            if ("payment_intent.succeeded".equals(event.getType())) {
//                PaymentIntent paymentIntent =
//                        (PaymentIntent) event.getDataObjectDeserializer()
//                                .getObject().orElseThrow();
//
//                String orderIdStr = paymentIntent.getMetadata().get("orderId");
//
//                if (orderIdStr != null) {
//                    Long orderId = Long.parseLong(orderIdStr);
//                    String token = "Bearer " + jwtUtil.generateToken(
//                            "PAYMENT-SERVICE", "PAYMENT-SERVICE", 0);
//
//                    orderService.placeOrder(orderId, token);
//                }
//            }
//            if ("payment_intent.payment_failed".equals(event.getType())) {
//                PaymentIntent paymentIntent =
//                        (PaymentIntent) event.getDataObjectDeserializer()
//                                .getObject().orElseThrow();
//
//                String orderIdStr = paymentIntent.getMetadata().get("orderId");
//
//                if (orderIdStr != null) {
//                    Long orderId = Long.parseLong(orderIdStr);
//                    String token = "Bearer " + jwtUtil.generateToken(
//                            "PAYMENT-SERVICE", "PAYMENT-SERVICE", 0);
//
//                    orderService.placeOrderFailed(orderId, token);
//                }
//            }
//
//            paymentService.markEventProcessed(eventId);
//        }catch (Exception e){
//            ResponseEntity.status(500).body("Webhook processing failed");
//        }
//
//        return ResponseEntity.ok("OK");
//    }
}