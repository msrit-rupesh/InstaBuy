package com.example.userservice.repository;

import com.example.userservice.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    List<OtpCode> findByEmail(String email);
    int deleteByCreatedAtBefore(Instant time);
}