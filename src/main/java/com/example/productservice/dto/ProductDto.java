package com.example.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Data transfer object representing a Product")
public class ProductDto {

    @Schema(description = "Unique identifier of the product", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Name of the product", example = "Wireless Noise-Canceling Headphones", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name is mandatory")
    private String name;

    @Schema(description = "Detailed product description", example = "High-fidelity Bluetooth over-ear headphones with ANC")
    private String description;

    @Schema(description = "Price of the product in USD", example = "199.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is mandatory")
    @PositiveOrZero(message = "Price must be zero or a positive value")
    private Double price;

    @Schema(description = "Quantity available in stock", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity is mandatory")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Category to which the product belongs", example = "Electronics", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category is mandatory")
    private String category;

    public ProductDto() {
    }

    public ProductDto(Long id, String name, String description, Double price, Integer quantity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
