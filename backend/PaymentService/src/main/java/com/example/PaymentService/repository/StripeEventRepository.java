package com.example.PaymentService.repository;

import com.example.PaymentService.model.StripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeEventRepository extends JpaRepository<StripeEvent,String> {

}
