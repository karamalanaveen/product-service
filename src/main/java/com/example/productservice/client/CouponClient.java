package com.example.productservice.client;

import com.example.productservice.dto.CouponResponseDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class CouponClient {

    private static final Logger logger = LoggerFactory.getLogger(CouponClient.class);

    private final RestTemplate restTemplate;

    @Value("${coupon-service.base-url:http://localhost:8082}")
    private String couponServiceBaseUrl;

    public CouponClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls coupon-service1 with Resilience4j Circuit Breaker protection.
     */
    @CircuitBreaker(name = "couponService", fallbackMethod = "getCouponFallback")
    public CouponResponseDto getCouponByCode(String code) {
        String url = couponServiceBaseUrl + "/api/coupons/code/" + code;
        logger.info("Calling coupon-service at: {}", url);
        return restTemplate.getForObject(url, CouponResponseDto.class);
    }

    /**
     * Fallback method matching the original signature + Throwable.
     * Handles CallNotPermittedException (when circuit is open) and downstream network/HTTP errors.
     */
    public CouponResponseDto getCouponFallback(String code, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            logger.warn("Resilience4j Circuit Breaker is OPEN for 'couponService'. Call not permitted for coupon '{}'. Cause: {}",
                    code, throwable.getMessage());
            return new CouponResponseDto(
                    code,
                    BigDecimal.ZERO,
                    true,
                    "Coupon service is currently unavailable (Circuit Breaker OPEN). Safe fallback applied with 0% discount."
            );
        }

        logger.error("Call to coupon-service failed for coupon '{}'. Error: {}. Fallback applied.",
                code, throwable.getMessage());
        return new CouponResponseDto(
                code,
                BigDecimal.ZERO,
                true,
                "Coupon service encountered an issue (" + throwable.getClass().getSimpleName() + "). Safe fallback applied with 0% discount."
        );
    }
}
