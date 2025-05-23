package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.CreateFakeStoreProductDto;
import com.aditi.backendcapstoneproject.dto.ErrorResponseDto;
import com.aditi.backendcapstoneproject.dto.ProductResponseDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductController {

    ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> getProductById
            (@PathVariable long id) throws ProductNotFoundException {

        Product product=productService.getProductsById(id);
        ProductResponseDto productResponseDto=ProductResponseDto.from(product);

        ResponseEntity<ProductResponseDto> responseEntity=new ResponseEntity(productResponseDto, HttpStatus.OK);
        return responseEntity;
    }


    @GetMapping("/products/")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(){

        List<Product> products = productService.getAllProducts();
        List<ProductResponseDto> productResponseDtos=new ArrayList<>();

        for(Product product: products){
            ProductResponseDto productResponseDto=ProductResponseDto.from(product);
            productResponseDtos.add(productResponseDto);
        }

        ResponseEntity<List<ProductResponseDto>> responseEntity=new ResponseEntity(productResponseDtos, HttpStatus.ACCEPTED);
        return responseEntity;
    }


    @PostMapping("/products")
    public ProductResponseDto createProduct(@RequestBody CreateFakeStoreProductDto createFakeStoreProductDto) {

        Product product=productService.createProduct(createFakeStoreProductDto.getName(),
                createFakeStoreProductDto.getDescription(),
                createFakeStoreProductDto.getCategory(),
                createFakeStoreProductDto.getPrice(),
                createFakeStoreProductDto.getImageUrl());

        ProductResponseDto productResponseDto=ProductResponseDto.from(product);
        return productResponseDto;
    }

}
