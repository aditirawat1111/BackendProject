package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Category;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.repository.CategoryRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("productDBService")
public class ProductDBService implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;

    ProductDBService(ProductRepository productRepository, CategoryRepository categoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Cacheable(cacheNames = "productsById", key = "#id")
    public Product getProductsById(Long id) throws ProductNotFoundException {
        Optional<Product> optionalProduct=productRepository.findById(id);
        if(optionalProduct.isEmpty()){
            throw new ProductNotFoundException("The product with id " + id + " is not found");
        }
        return optionalProduct.get();
    }

    @Override
    @Cacheable(cacheNames = "productsAll")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "productsAll", allEntries = true),
            @CacheEvict(cacheNames = "productsById", allEntries = true),
            @CacheEvict(cacheNames = "productsByCategory", allEntries = true),
            @CacheEvict(cacheNames = "productsSearch", allEntries = true)
    })
    public Product createProduct(String name, String description, String category, Double price, String imageUrl) {

        Product product=new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageUrl(imageUrl);

        Category category1=getCategoryFromDB(category);
        product.setCategory(category1);
        return productRepository.save(product);

    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "productsAll", allEntries = true),
            @CacheEvict(cacheNames = "productsById", key = "#id"),
            @CacheEvict(cacheNames = "productsByCategory", allEntries = true),
            @CacheEvict(cacheNames = "productsSearch", allEntries = true)
    })
    public Product updateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException {
        Product product=productRepository.findById(id)
                .orElseThrow(()->new ProductNotFoundException("The Product with id "+id+" doesn't exist"));

        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());

        product.setPrice(productRequestDto.getPrice()!=null ? productRequestDto.getPrice() : 0.0);
        product.setImageUrl(productRequestDto.getImageUrl());

        Category category1=getCategoryFromDB(productRequestDto.getCategory());
        product.setCategory(category1);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "productsAll", allEntries = true),
            @CacheEvict(cacheNames = "productsById", key = "#id"),
            @CacheEvict(cacheNames = "productsByCategory", allEntries = true),
            @CacheEvict(cacheNames = "productsSearch", allEntries = true)
    })
    public Product partialUpdateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException {
        Product product=productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("The Product with id "+id+" doesn't exist"));

        if(productRequestDto.getName()!=null){
            product.setName(productRequestDto.getName());
        }
        if(productRequestDto.getDescription()!=null){
            product.setDescription(productRequestDto.getDescription());
        }
        if(productRequestDto.getPrice()!=null){
            product.setPrice(productRequestDto.getPrice());
        }
        if(productRequestDto.getImageUrl()!=null){
            product.setImageUrl(productRequestDto.getImageUrl());
        }
        if(productRequestDto.getCategory()!=null) {
            Category category = getCategoryFromDB(productRequestDto.getCategory());
            product.setCategory(category);
        }
        return productRepository.save(product);
    }


    @Override
    @Cacheable(cacheNames = "productsSearch", key = "#keyword == null ? 'ALL' : #keyword.trim().toLowerCase()")
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchProducts(keyword.trim(), Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchProducts(keyword.trim(), pageable);
    }

    @Override
    @Cacheable(cacheNames = "productsByCategory", key = "#categoryName == null ? 'ALL' : #categoryName.trim().toLowerCase()")
    public List<Product> getProductsByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByCategory_Name(categoryName.trim());
    }

    @Override
    public Page<Product> getProductsByCategory(String categoryName, Pageable pageable) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByCategory_Name(categoryName.trim(), pageable);
    }

    public Category getCategoryFromDB(String name){
        if(name == null || name.trim().isEmpty()){
            throw new NullPointerException("Category name cannot be null or empty");
        }
        Optional<Category> optionalCategory = categoryRepository.findByName(name);
        if(optionalCategory.isPresent()){
            return optionalCategory.get();
        }
        Category category=new Category();
        category.setName(name);
        categoryRepository.save(category);
        return category;
    }
}
