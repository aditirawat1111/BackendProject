package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.FakeStoreProductDto;
import com.aditi.backendcapstoneproject.dto.FakeStoreProductRequestDto;
import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service("fakeStoreProductService")
public class FakeStoreProductService implements ProductService{

    RestTemplate restTemplate;

    public FakeStoreProductService(RestTemplate restTemplate){
        this.restTemplate=restTemplate;
    }

    @Override
    public Product getProductsById(Long id) throws ProductNotFoundException {
        FakeStoreProductDto fakeStoreProductDto=restTemplate.getForObject(
                "https://fakestoreapi.com/products/" + id, FakeStoreProductDto.class);

        if(fakeStoreProductDto==null){
            throw new ProductNotFoundException("The product for id " + id + " does not exist");
        }

        return fakeStoreProductDto.toProduct();

    }

    @Override
    public List<Product> getAllProducts() {
        FakeStoreProductDto[] fakeStoreProductDtos=restTemplate.getForObject(
                "https://fakestoreapi.com/products", FakeStoreProductDto[].class
        );

        List<Product> products=new ArrayList<>();

        for(FakeStoreProductDto fakeStoreProductDto:fakeStoreProductDtos){
            Product product=fakeStoreProductDto.toProduct();
            products.add(product);
        }
        return products;
    }

    @Override
    public Product createProduct(String title, String description, String category, double price, String image) {

        FakeStoreProductRequestDto fakeStoreProductRequestDto=new FakeStoreProductRequestDto();
        fakeStoreProductRequestDto.setTitle(title);
        fakeStoreProductRequestDto.setDescription(description);
        fakeStoreProductRequestDto.setCategory(category);
        fakeStoreProductRequestDto.setPrice(price);
        fakeStoreProductRequestDto.setImage(image);

        FakeStoreProductDto fakeStoreProductDto=restTemplate.postForObject("https://fakestoreapi.com/products",
                fakeStoreProductRequestDto, FakeStoreProductDto.class);

        return fakeStoreProductDto.toProduct();
    }

    @Override
    public Product updateProduct(Long id, ProductRequestDto productRequestDto) {
        return null;
    }

    @Override
    public Product partialUpdateProduct(Long id, ProductRequestDto productRequestDto) {
        return null;
    }
}
