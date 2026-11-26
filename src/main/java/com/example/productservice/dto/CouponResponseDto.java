package com.example.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CouponResponseDto {

    private Long id;
    private String code;
    private BigDecimal discount;
    private LocalDate expDate;
    private boolean isFallback;
    private String fallbackMessage;

    public CouponResponseDto() {
    }

    public CouponResponseDto(Long id, String code, BigDecimal discount, LocalDate expDate) {
        this.id = id;
        this.code = code;
        this.discount = discount;
        this.expDate = expDate;
        this.isFallback = false;
    }

    public CouponResponseDto(String code, BigDecimal discount, boolean isFallback, String fallbackMessage) {
        this.code = code;
        this.discount = discount;
        this.isFallback = isFallback;
        this.fallbackMessage = fallbackMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public boolean isFallback() {
        return isFallback;
    }

    public void setFallback(boolean fallback) {
        isFallback = fallback;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }

    public void setFallbackMessage(String fallbackMessage) {
        this.fallbackMessage = fallbackMessage;
    }
}
