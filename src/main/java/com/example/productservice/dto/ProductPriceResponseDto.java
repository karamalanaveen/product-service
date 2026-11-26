package com.example.productservice.dto;

import java.math.BigDecimal;

public class ProductPriceResponseDto {

    private Long productId;
    private String productName;
    private Double originalPrice;
    private BigDecimal discountApplied;
    private BigDecimal finalPrice;
    private String couponCode;
    private boolean fallbackApplied;
    private String statusMessage;

    public ProductPriceResponseDto() {
    }

    public ProductPriceResponseDto(Long productId, String productName, Double originalPrice,
                                   BigDecimal discountApplied, BigDecimal finalPrice,
                                   String couponCode, boolean fallbackApplied, String statusMessage) {
        this.productId = productId;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.discountApplied = discountApplied;
        this.finalPrice = finalPrice;
        this.couponCode = couponCode;
        this.fallbackApplied = fallbackApplied;
        this.statusMessage = statusMessage;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public boolean isFallbackApplied() {
        return fallbackApplied;
    }

    public void setFallbackApplied(boolean fallbackApplied) {
        this.fallbackApplied = fallbackApplied;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
