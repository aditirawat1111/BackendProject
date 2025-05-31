package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Category;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.repository.CategoryRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
    public Product getProductsById(Long id) throws ProductNotFoundException {
        Optional<Product> optionalProduct=productRepository.findById(id);
        if(optionalProduct.isEmpty()){
            throw new ProductNotFoundException("The product with id " + id + " is not found");
        }
        return optionalProduct.get();
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(String name, String description, String category, double price, String imageUrl) {

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
    public Product updateProduct(Long id, ProductRequestDto productRequestDto) {
        Product product=new Product();
        product.setId(id);
        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        product.setImageUrl(productRequestDto.getImageUrl());

        Category category1=getCategoryFromDB(productRequestDto.getCategory());
        product.setCategory(category1);
        return productRepository.save(product);
    }

    @Override
    public Product partialUpdateProduct(Long id, ProductRequestDto productRequestDto) throws ProductNotFoundException {
        Product product=productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("The Product with id "+id+" doesn't exist."));

        if(productRequestDto.getName()!=null && !productRequestDto.getName().equals(product.getName())){
            product.setName(productRequestDto.getName());
        }
        if(productRequestDto.getDescription()!=null && !productRequestDto.getDescription().equals(product.getDescription())){
            product.setDescription(productRequestDto.getDescription());
        }
        if(productRequestDto.getPrice()!=0 && productRequestDto.getPrice()!=(product.getPrice())){
            product.setPrice(productRequestDto.getPrice());
        }
        if(productRequestDto.getImageUrl()!=null && productRequestDto.getImageUrl().equals(product.getImageUrl())){
            product.setImageUrl(productRequestDto.getImageUrl());
        }
        if(productRequestDto.getCategory()!=null && !productRequestDto.getCategory().equals(product.getCategory().getName())) {
            Category category = getCategoryFromDB(productRequestDto.getCategory());
            product.setCategory(category);
        }
        return productRepository.save(product);
    }


    public Category getCategoryFromDB(String name){
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
