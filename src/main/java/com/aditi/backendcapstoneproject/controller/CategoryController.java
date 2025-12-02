package com.aditi.backendcapstoneproject.controller;

import com.aditi.backendcapstoneproject.dto.CategoryResponseDto;
import com.aditi.backendcapstoneproject.dto.ProductResponseDto;
import com.aditi.backendcapstoneproject.model.Category;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.repository.CategoryRepository;
import com.aditi.backendcapstoneproject.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public CategoryController(CategoryRepository categoryRepository,
                              ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponseDto> categoryDtos = categories.stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(categoryDtos, HttpStatus.OK);
    }

    @GetMapping("/{name}/products")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategoryName(
            @PathVariable("name") String categoryName) {
        List<Product> products = productService.getProductsByCategory(categoryName);
        List<ProductResponseDto> productDtos = products.stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }
}


