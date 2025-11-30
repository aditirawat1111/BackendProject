package com.aditi.backendcapstoneproject.repository;

import com.aditi.backendcapstoneproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//JPA repository:
//1st argument=table/model name;
//2nd argument=type of the primary key for the table;
public interface ProductRepository extends JpaRepository<Product, Long> {

    Product save(Product product);

    List<Product> findAll();

    Optional<Product> findById(long id);

    //Declarative Queries
    List<Product> findByCategory_Name(String categoryName);

    //HQL Queries
    @Query("select p from Product p where p.category.name=:categoryName")
    List<Product> getProductByCategoryName(@Param("categoryName") String categoryName);

    //Native Queries
    @Query(value=CustomQuery.GET_PRODUCT_FROM_CATEGORY_NAME, nativeQuery = true)
    List<Product> getProductByCategoryNameNative(@Param("categoryName") String categoryName);

    //Search Queries
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :keyword, '%')) or lower(p.description) like lower(concat('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);
}
