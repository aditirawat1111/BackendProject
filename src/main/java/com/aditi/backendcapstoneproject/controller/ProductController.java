package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.dto.ProductResponseDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Products", description = "APIs for product management - browse, search, and manage products")
public class ProductController {

    ProductService productService;

    public ProductController(@Qualifier("productDBService")
                             ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
            @Parameter(description = "Product ID", required = true) @PathVariable long id) 
            throws ProductNotFoundException {

        Product product = productService.getProductsById(id);
        ProductResponseDto productResponseDto = ProductResponseDto.from(product);

        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }


    @Operation(summary = "Get all products", description = "Retrieves a paginated list of products with optional filtering by category or search query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'id,asc')") @RequestParam(defaultValue = "id,asc") String sort,
            @Parameter(description = "Filter by category name") @RequestParam(required = false) String category,
            @Parameter(description = "Search query") @RequestParam(required = false) String q) {

        Pageable pageable = buildPageable(page, size, sort);

        Page<Product> productPage;
        if (q != null && !q.trim().isEmpty()) {
            productPage = productService.searchProducts(q, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            productPage = productService.getProductsByCategory(category, pageable);
        } else {
            productPage = productService.getAllProducts(pageable);
        }

        Page<ProductResponseDto> dtoPage = productPage.map(ProductResponseDto::from);

        return new ResponseEntity<>(dtoPage, HttpStatus.OK);
    }

    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<Product> productPage = productService.searchProducts(q, pageable);
        Page<ProductResponseDto> dtoPage = productPage.map(ProductResponseDto::from);

        return new ResponseEntity<>(dtoPage, HttpStatus.OK);
    }

    @GetMapping("/products/by-category")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @RequestParam("category") String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<Product> productPage = productService.getProductsByCategory(categoryName, pageable);
        Page<ProductResponseDto> dtoPage = productPage.map(ProductResponseDto::from);

        return new ResponseEntity<>(dtoPage, HttpStatus.OK);
    }


    @Operation(summary = "Create a new product", description = "Creates a new product in the system (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productRequestDto) {

        Product product=productService.createProduct(productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getCategory(),
                productRequestDto.getPrice(),
                productRequestDto.getImageUrl());

        ProductResponseDto productResponseDto=ProductResponseDto.from(product);
        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }


    @Operation(summary = "Update a product", description = "Updates an existing product (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable long id,  @RequestBody  ProductRequestDto productRequestDto) throws ProductNotFoundException{
        Product product=productService.updateProduct(id, productRequestDto);
        ProductResponseDto productResponseDto=ProductResponseDto.from(product);
        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }


    @Operation(summary = "Partially update a product", description = "Partially updates an existing product (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> partialUpdateProduct
            (@PathVariable Long id
                    ,@RequestBody ProductRequestDto productRequestDto) throws ProductNotFoundException {

        Product updateProduct=productService.partialUpdateProduct(id, productRequestDto);
        ProductResponseDto productResponseDto=ProductResponseDto.from(updateProduct);

        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1) {
            direction = Sort.Direction.fromString(sortParts[1]);
        }
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}