package com.example.userservice.service;

import com.example.userservice.repository.OtpCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpCleanUpService {

    private final OtpCodeRepository otpCodeRepository;

    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initCleanupOnce() {
        cleanupExpiredOtps();
    }



     @Scheduled(fixedRate = 3_600_000)
     @Transactional
     public void hourlyCleanup() {
         cleanupExpiredOtps();
     }

    public void cleanupExpiredOtps() {
        Instant threshold = Instant.now().minus(OTP_TTL);
        int deleted = otpCodeRepository.deleteByCreatedAtBefore(threshold);
        log.info("OTP cleanup: deleted {} rows older than {}", deleted, threshold);
    }
}