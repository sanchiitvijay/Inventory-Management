package com.microservices.product.service;

import com.microservices.product.entity.Product;
import com.microservices.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setSku("TEST-SKU-001");
        testProduct.setDescription("Test Description");
        testProduct.setRecommendedRetailPrice(new BigDecimal("99.99"));
    }

    @Test
    void testCreateProduct_Success() {
        // Arrange
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("TEST-SKU-001", result.getSku());
        verify(productRepository, times(1)).existsBySku("TEST-SKU-001");
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void testCreateProduct_DuplicateSku_ThrowsException() {
        // Arrange
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(testProduct)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setSku("TEST-SKU-002");
        
        List<Product> productList = Arrays.asList(testProduct, product2);
        when(productRepository.findAll()).thenReturn(productList);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        // Act
        Optional<Product> result = productService.getProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testGetProductBySku_Success() {
        // Arrange
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(testProduct));

        // Act
        Optional<Product> result = productService.getProductBySku("TEST-SKU-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TEST-SKU-001", result.get().getSku());
        verify(productRepository, times(1)).findBySku("TEST-SKU-001");
    }

    @Test
    void testUpdateProduct_Success() {
        // Arrange
        Product updatedDetails = new Product();
        updatedDetails.setName("Updated Product");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setRecommendedRetailPrice(new BigDecimal("149.99"));

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void testUpdateProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(999L, testProduct)
        );
        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_DuplicateSku_ThrowsException() {
        // Arrange
        Product updatedDetails = new Product();
        updatedDetails.setSku("DUPLICATE-SKU");

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.existsBySku("DUPLICATE-SKU")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(1L, updatedDetails)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(999L)
        );
        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository, never()).deleteById(anyLong());
    }
}
