package com.example.productservice.controller;

import com.example.productservice.client.CouponClient;
import com.example.productservice.dto.CouponResponseDto;
import com.example.productservice.dto.ErrorResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.dto.ProductPriceResponseDto;
import com.example.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Tag(name = "Product Management", description = "CRUD operations and pricing calculations for products")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CouponClient couponClient;

    public ProductController(ProductService productService, CouponClient couponClient) {
        this.productService = productService;
        this.couponClient = couponClient;
    }

    // 1. Create Product -> 201 CREATED
    @Operation(summary = "Create a new product", description = "Saves a new product in the database and returns the created product details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // 2. Get All Products -> 200 OK
    @Operation(summary = "Retrieve all products", description = "Fetches a list of all products currently available in the catalog.")
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductDto.class))))
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // 3. Get Product by ID -> 200 OK (or 404 NOT FOUND via exception handler)
    @Operation(summary = "Get product by ID", description = "Retrieves a single product's details using its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "ID of the product to retrieve", example = "1")
            @PathVariable("id") Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // 4. Update Product -> 200 OK (or 404 NOT FOUND via exception handler)
    @Operation(summary = "Update an existing product", description = "Updates fields of an existing product by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "ID of the product to update", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    // 5. Delete Product -> 200 OK (or 404 NOT FOUND via exception handler)
    @Operation(summary = "Delete product by ID", description = "Removes a product from the database by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @Parameter(description = "ID of the product to delete", example = "1")
            @PathVariable("id") Long id) {
        productService.deleteProduct(id);
        Map<String, Object> response = Map.of(
                "message", "Product deleted successfully with id: " + id,
                "status", HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // 6. Calculate Product Price with Coupon (Protected by Resilience4j Circuit Breaker)
    @Operation(summary = "Calculate product price with optional coupon",
            description = "Calculates the final discounted price by calling coupon-service1 with Resilience4j circuit-breaker protection.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price calculation completed (or safe fallback applied)",
                    content = @Content(schema = @Schema(implementation = ProductPriceResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/price")
    public ResponseEntity<ProductPriceResponseDto> getProductPriceWithCoupon(
            @Parameter(description = "ID of the product", example = "1")
            @PathVariable("id") Long id,
            @Parameter(description = "Optional coupon code (e.g. SUMMER20)", example = "SUMMER20")
            @RequestParam(name = "couponCode", required = false) String couponCode) {

        ProductDto product = productService.getProductById(id);
        BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());

        if (couponCode == null || couponCode.trim().isEmpty()) {
            return ResponseEntity.ok(new ProductPriceResponseDto(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    BigDecimal.ZERO,
                    originalPrice,
                    null,
                    false,
                    "No coupon code provided. Standard price applied."
            ));
        }

        // Call coupon-service via Resilience4j protected client
        CouponResponseDto couponResponse = couponClient.getCouponByCode(couponCode);

        BigDecimal discount = couponResponse.getDiscount() != null
                ? couponResponse.getDiscount()
                : BigDecimal.ZERO;

        // Calculate final price: price - (price * discount / 100)
        BigDecimal discountAmount = originalPrice
                .multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = originalPrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        String message = couponResponse.isFallback()
                ? couponResponse.getFallbackMessage()
                : "Coupon '" + couponCode + "' successfully applied with " + discount + "% discount.";

        return ResponseEntity.ok(new ProductPriceResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                discount,
                finalPrice,
                couponCode,
                couponResponse.isFallback(),
                message
        ));
    }
}
