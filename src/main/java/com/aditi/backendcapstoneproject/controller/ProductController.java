package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.dto.ProductResponseDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.service.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

        Product product=productService.getProductsById(id);
        ProductResponseDto productResponseDto=ProductResponseDto.from(product);

        return new ResponseEntity(productResponseDto, HttpStatus.OK);
    }


    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(){

        List<Product> products = productService.getAllProducts();
//        List<ProductResponseDto> productResponseDtos=new ArrayList<>();

        List<ProductResponseDto> productResponseDtos=
                products.stream()
                        .map(ProductResponseDto::from)
                        .collect(Collectors.toList());

//        for(Product product: products){
//            ProductResponseDto productResponseDto=ProductResponseDto.from(product);
//            productResponseDtos.add(productResponseDto);
//        }

        return new ResponseEntity(productResponseDtos, HttpStatus.ACCEPTED);
    }


    @PostMapping("/products/")
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
}