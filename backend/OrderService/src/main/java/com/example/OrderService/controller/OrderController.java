package com.example.OrderService.controller;

import com.example.OrderService.dto.AuthUser;
import com.example.OrderService.dto.OrderDTO;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.exception.NotFoundException;
import com.example.OrderService.model.Order;
import com.example.OrderService.security.JwtUtil;
import com.example.OrderService.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwt;

    public OrderController(OrderService orderService,JwtUtil jwt){
        this.orderService=orderService;
        this.jwt=jwt;
    }

    @GetMapping
    public String greet(){
        return "Hello World";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody List<OrderItemDTO> orderItems, Authentication authentication)
    {
        try{
            AuthUser authUser=(AuthUser) authentication.getPrincipal();
            String token=jwt.generateToken("ORDER-SERVICE","ORDER-SERVICE",0);
            String authToken="Bearer "+token;
            Order order=orderService.generateOrder(authUser.getId(),orderItems,authToken);
            return ResponseEntity.ok().body(order);
        }catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, Authentication authentication)
    {
        try{
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            Order order=orderService.getOrderById(id);
            return ResponseEntity.ok().body(order);
        }catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/get-all-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrderByUsers(Authentication authentication)
    {
        try{
            AuthUser authUser=(AuthUser)authentication.getPrincipal();
            List<Order> orders=orderService.getOrderByUser(authUser.getId());
            return ResponseEntity.ok().body(orders);
        }catch (NotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}