package com.example.productservice.service;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Laptop", "High performance laptop", 1200.0, 10, "Electronics");
        productDto = new ProductDto(1L, "Laptop", "High performance laptop", 1200.0, 10, "Electronics");
    }

    @Test
    @DisplayName("Create Product - Happy Path")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto savedDto = productService.createProduct(productDto);

        assertNotNull(savedDto);
        assertEquals(product.getId(), savedDto.getId());
        assertEquals(product.getName(), savedDto.getName());
        assertEquals(product.getPrice(), savedDto.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Get All Products - Happy Path")
    void getAllProducts_Success() {
        Product product2 = new Product(2L, "Mouse", "Wireless Mouse", 25.0, 50, "Accessories");
        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

        List<ProductDto> productList = productService.getAllProducts();

        assertNotNull(productList);
        assertEquals(2, productList.size());
        assertEquals("Laptop", productList.get(0).getName());
        assertEquals("Mouse", productList.get(1).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get Product By ID - Existing ID")
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDto foundDto = productService.getProductById(1L);

        assertNotNull(foundDto);
        assertEquals(1L, foundDto.getId());
        assertEquals("Laptop", foundDto.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get Product By ID - Not Found Throws Exception")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(99L)
        );

        assertNotNull(exception);
        assertEquals("Product not found with id : '99'", exception.getMessage());
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Update Product - Existing ID")
    void updateProduct_Success() {
        ProductDto updatePayload = new ProductDto(null, "Gaming Laptop", "Updated description", 1400.0, 5, "Gaming");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto updatedDto = productService.updateProduct(1L, updatePayload);

        assertNotNull(updatedDto);
        assertEquals("Gaming Laptop", product.getName());
        assertEquals(1400.0, product.getPrice());
        assertEquals(5, product.getQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Update Product - Not Found Throws Exception")
    void updateProduct_NotFound_ThrowsException() {
        ProductDto updatePayload = new ProductDto(null, "Gaming Laptop", "Updated", 1400.0, 5, "Gaming");

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, updatePayload)
        );

        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete Product - Existing ID")
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Delete Product - Not Found Throws Exception")
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(99L)
        );

        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).delete(any(Product.class));
    }
}
