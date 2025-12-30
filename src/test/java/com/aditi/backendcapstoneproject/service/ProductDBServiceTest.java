package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Category;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.repository.CategoryRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductDBServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductDBService productDBService;

    private Product testProduct;
    private Category testCategory;
    private ProductRequestDto productRequestDto;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setDescription("High performance laptop");
        testProduct.setPrice(999.99);
        testProduct.setImageUrl("https://example.com/laptop.jpg");
        testProduct.setCategory(testCategory);

        productRequestDto = new ProductRequestDto();
        productRequestDto.setName("Updated Laptop");
        productRequestDto.setDescription("Updated description");
        productRequestDto.setPrice(1099.99);
        productRequestDto.setImageUrl("https://example.com/updated-laptop.jpg");
        productRequestDto.setCategory("Electronics");

    }

    @Test
    void testGetProductById_Success() throws ProductNotFoundException {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Product result = productDBService.getProductsById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productDBService.getProductsById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("The product with id 999 is not found");
    }

    @Test
    void testGetAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productDBService.getAllProducts();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetAllProducts_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // When
        Page<Product> result = productDBService.getAllProducts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void testCreateProduct_WithExistingCategory() {
        // Given
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productDBService.createProduct(
                "Laptop", "High performance laptop", "Electronics", 999.99, "https://example.com/laptop.jpg"
        );

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository, times(1)).findByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_WithNewCategory() {
        // Given
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Books");

        when(categoryRepository.findByName("Books")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productDBService.createProduct(
                "Laptop", "High performance laptop", "Books", 999.99, "https://example.com/laptop.jpg"
        );

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository, times(1)).findByName("Books");
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_WhenCategoryIsNull() {
        // Given - no need to mock since exception will be thrown before repository calls

        // When & Then
        assertThatThrownBy(() ->
                productDBService.createProduct("A", "B", null, 10.0, "url")
        ).isInstanceOf(NullPointerException.class)
         .hasMessageContaining("Category name cannot be null or empty");
    }

    @Test
    void testUpdateProduct_Success() throws ProductNotFoundException {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setDescription("Updated description");
        updatedProduct.setPrice(1099.99);
        updatedProduct.setImageUrl("https://example.com/updated-laptop.jpg");
        updatedProduct.setCategory(testCategory);


        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productDBService.updateProduct(1L, productRequestDto);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productDBService.updateProduct(999L, productRequestDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("The Product with id 999 doesn't exist");
    }

    @Test
    void testPartialUpdateProduct_Success() throws ProductNotFoundException {
        // Given
        ProductRequestDto partialDto = new ProductRequestDto();
        partialDto.setName("Partially Updated Laptop");
        partialDto.setPrice(1199.99);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Partially Updated Laptop");
        updatedProduct.setDescription("High performance laptop");
        updatedProduct.setPrice(1199.99);
        updatedProduct.setImageUrl("https://example.com/laptop.jpg");
        updatedProduct.setCategory(testCategory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productDBService.partialUpdateProduct(1L, partialDto);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testPartialUpdateProduct_NotFound() {
        // Given
        ProductRequestDto partialDto = new ProductRequestDto();
        partialDto.setName("Updated Name");

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productDBService.partialUpdateProduct(999L, partialDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("The Product with id 999 doesn't exist");
    }

    @Test
    void testSearchProducts_WithKeyword() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        when(productRepository.searchProducts("laptop", pageable)).thenReturn(productPage);

        // When
        List<Product> result = productDBService.searchProducts("laptop");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        verify(productRepository, times(1)).searchProducts("laptop", Pageable.unpaged());
    }

    @Test
    void testSearchProducts_WithEmptyKeyword() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productDBService.searchProducts("");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testSearchProducts_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageable, 1);
        when(productRepository.searchProducts("laptop", pageable)).thenReturn(productPage);

        // When
        Page<Product> result = productDBService.searchProducts("laptop", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        verify(productRepository, times(1)).searchProducts("laptop", pageable);
    }

    @Test
    void testGetProductsByCategory() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory_Name("Electronics")).thenReturn(products);

        // When
        List<Product> result = productDBService.getProductsByCategory("Electronics");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        verify(productRepository, times(1)).findByCategory_Name("Electronics");
    }

    @Test
    void testGetProductsByCategory_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageable, 1);
        when(productRepository.findByCategory_Name("Electronics", pageable)).thenReturn(productPage);

        // When
        Page<Product> result = productDBService.getProductsByCategory("Electronics", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        verify(productRepository, times(1)).findByCategory_Name("Electronics", pageable);
    }
}

