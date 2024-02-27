package dev.magadiflo.swagger.app.service;

import dev.magadiflo.swagger.app.persistence.entity.Product;

import java.util.List;

public interface IProductService {
    List<Product> findAllProducts();

    Product findProductById(Long id);

    Product saveProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProductById(Long id);
}
