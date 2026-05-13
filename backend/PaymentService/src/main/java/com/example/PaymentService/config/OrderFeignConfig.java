package com.example.PaymentService.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderFeignConfig {

    @Bean
    public Client feignClient() {
        return new ApacheHttpClient();
    }
}