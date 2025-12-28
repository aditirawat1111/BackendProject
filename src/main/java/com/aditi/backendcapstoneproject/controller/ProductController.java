package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.dto.ProductResponseDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    ProductService productService;

    public ProductController(@Qualifier("productDBService")
                             ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> getProductById
            (@PathVariable long id) throws ProductNotFoundException {

        Product product = productService.getProductsById(id);
        ProductResponseDto productResponseDto = ProductResponseDto.from(product);

        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }


    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q) {

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


    @PostMapping("/products")
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto productRequestDto) {

        Product product=productService.createProduct(productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getCategory(),
                productRequestDto.getPrice(),
                productRequestDto.getImageUrl());

        ProductResponseDto productResponseDto=ProductResponseDto.from(product);
        return productResponseDto;
    }


    @PutMapping("/products/{id}")
    public ProductResponseDto updateProduct(@PathVariable long id,  @RequestBody  ProductRequestDto productRequestDto) throws ProductNotFoundException{
        Product product=productService.updateProduct(id, productRequestDto);
        ProductResponseDto productResponseDto=ProductResponseDto.from(product);
        return productResponseDto;
    }


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