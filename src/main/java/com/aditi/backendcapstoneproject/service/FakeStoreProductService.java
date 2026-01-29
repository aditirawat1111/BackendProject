package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.FakeStoreProductDto;
import com.aditi.backendcapstoneproject.dto.FakeStoreProductRequestDto;
import com.aditi.backendcapstoneproject.dto.ProductRequestDto;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    @Cacheable(cacheNames = "fakestoreProductsById", key = "#id")
    public Product getProductsById(Long id) throws ProductNotFoundException {
        FakeStoreProductDto fakeStoreProductDto=restTemplate.getForObject(
                "https://fakestoreapi.com/products/" + id, FakeStoreProductDto.class);

        if(fakeStoreProductDto==null){
            throw new ProductNotFoundException("The product for id " + id + " does not exist");
        }

        return fakeStoreProductDto.toProduct();

    }

    @Override
    @Cacheable(cacheNames = "fakestoreProductsAll")
    public List<Product> getAllProducts() {
        FakeStoreProductDto[] fakeStoreProductDtos = restTemplate.getForObject(
                "https://fakestoreapi.com/products", FakeStoreProductDto[].class
        );

        List<Product> products = new ArrayList<>();

        if (fakeStoreProductDtos != null) {
            for (FakeStoreProductDto fakeStoreProductDto : fakeStoreProductDtos) {
                if (fakeStoreProductDto != null) {
                    Product product = fakeStoreProductDto.toProduct();
                    products.add(product);
                }
            }
        }
        return products;
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        List<Product> all = getAllProducts();
        return paginateList(all, pageable);
    }

    @Override
    public Product createProduct(String title, String description, String category, Double price, String image) {

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
        throw new UnsupportedOperationException("Update is not supported for FakeStoreProductService");
    }

    @Override
    public Product partialUpdateProduct(Long id, ProductRequestDto productRequestDto) {
        throw new UnsupportedOperationException("Partial update is not supported for FakeStoreProductService");
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        List<Product> allProducts = getAllProducts();
        if (keyword == null || keyword.trim().isEmpty()) {
            return allProducts;
        }
        
        String lowerKeyword = keyword.toLowerCase().trim();
        return allProducts.stream()
                .filter(product -> 
                    (product.getName() != null && product.getName().toLowerCase().contains(lowerKeyword)) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerKeyword))
                )
                .toList();
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        List<Product> filtered = searchProducts(keyword);
        return paginateList(filtered, pageable);
    }

    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        // FakeStore API does not support categories in this implementation;
        // return all products for now.
        return getAllProducts();
    }

    @Override
    public Page<Product> getProductsByCategory(String categoryName, Pageable pageable) {
        return getAllProducts(pageable);
    }

    private Page<Product> paginateList(List<Product> products, Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(products);
        }

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        if (startItem >= products.size()) {
            return new PageImpl<>(List.of(), pageable, products.size());
        }

        int toIndex = Math.min(startItem + pageSize, products.size());
        List<Product> subList = products.subList(startItem, toIndex);

        return new PageImpl<>(subList, pageable, products.size());
    }
}
