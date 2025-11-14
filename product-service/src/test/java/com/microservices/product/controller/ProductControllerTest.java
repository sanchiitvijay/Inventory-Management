package com.microservices.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.product.entity.Product;
import com.microservices.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void testCreateProduct_Success() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.sku", is("TEST-SKU-001")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.recommendedRetailPrice", is(99.99)));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void testCreateProduct_DuplicateSku_ReturnsBadRequest() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product with SKU TEST-SKU-001 already exists"));

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("already exists")));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void testGetAllProducts_Success() throws Exception {
        // Arrange
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setSku("TEST-SKU-002");
        product2.setDescription("Description 2");
        product2.setRecommendedRetailPrice(new BigDecimal("149.99"));

        List<Product> productList = Arrays.asList(testProduct, product2);
        when(productService.getAllProducts()).thenReturn(productList);

        // Act & Assert
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Product")))
                .andExpect(jsonPath("$[1].name", is("Product 2")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testGetAllProducts_EmptyList() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testGetProductById_Success() throws Exception {
        // Arrange
        when(productService.getProductById(anyLong())).thenReturn(Optional.of(testProduct));

        // Act & Assert
        mockMvc.perform(get("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.sku", is("TEST-SKU-001")));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        // Arrange
        when(productService.getProductById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("not found")));

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setSku("TEST-SKU-001");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setRecommendedRetailPrice(new BigDecimal("149.99"));

        when(productService.updateProduct(anyLong(), any(Product.class))).thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.recommendedRetailPrice", is(149.99)));

        verify(productService, times(1)).updateProduct(anyLong(), any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound_ReturnsBadRequest() throws Exception {
        // Arrange
        when(productService.updateProduct(anyLong(), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("not found")));

        verify(productService, times(1)).updateProduct(anyLong(), any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("deleted successfully")));

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void testDeleteProduct_NotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Product not found with id: 999"))
                .when(productService).deleteProduct(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("not found")));

        verify(productService, times(1)).deleteProduct(999L);
    }
}
