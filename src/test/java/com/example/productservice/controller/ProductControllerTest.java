package com.example.productservice.controller;

import com.example.productservice.client.CouponClient;
import com.example.productservice.dto.CouponResponseDto;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private CouponClient couponClient;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productDto = new ProductDto(1L, "Wireless Headphones", "High quality headphones", 199.99, 30, "Electronics");
    }

    @Test
    @DisplayName("POST /api/products - Should return 201 Created when valid")
    void createProduct_Returns201Created() throws Exception {
        when(productService.createProduct(any(ProductDto.class))).thenReturn(productDto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Wireless Headphones")))
                .andExpect(jsonPath("$.price", is(199.99)))
                .andExpect(jsonPath("$.quantity", is(30)));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 Bad Request when validation fails")
    void createProduct_ValidationError_Returns400BadRequest() throws Exception {
        ProductDto invalidDto = new ProductDto(null, "", "No name", -50.0, -2, "");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.quantity").exists());
    }

    @Test
    @DisplayName("GET /api/products - Should return 200 OK and all products")
    void getAllProducts_Returns200OK() throws Exception {
        ProductDto product2 = new ProductDto(2L, "USB Cable", "Type-C cable", 9.99, 100, "Accessories");
        when(productService.getAllProducts()).thenReturn(Arrays.asList(productDto, product2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Wireless Headphones")))
                .andExpect(jsonPath("$[1].name", is("USB Cable")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 200 OK when product exists")
    void getProductById_Returns200OK() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Wireless Headphones")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 404 Not Found when product does not exist")
    void getProductById_NotFound_Returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Product not found with id : '99'")));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should return 200 OK when updated")
    void updateProduct_Returns200OK() throws Exception {
        ProductDto updatedDto = new ProductDto(1L, "Pro Headphones", "ANC version", 249.99, 20, "Electronics");
        when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Pro Headphones")))
                .andExpect(jsonPath("$.price", is(249.99)));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should return 200 OK")
    void deleteProduct_Returns200OK() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Product deleted successfully with id: 1")))
                .andExpect(jsonPath("$.status", is(200)));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should return 404 Not Found when missing")
    void deleteProduct_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Product", "id", 99L)).when(productService).deleteProduct(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/products/{id}/price - Should return 200 OK with discount calculation")
    void getProductPriceWithCoupon_Returns200OK() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDto);
        when(couponClient.getCouponByCode("SUMMER20")).thenReturn(
                new CouponResponseDto(1L, "SUMMER20", BigDecimal.valueOf(20.0), null)
        );

        mockMvc.perform(get("/api/products/1/price")
                        .param("couponCode", "SUMMER20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.discountApplied", is(20.0)))
                .andExpect(jsonPath("$.finalPrice", is(159.99)))
                .andExpect(jsonPath("$.fallbackApplied", is(false)));
    }
}
