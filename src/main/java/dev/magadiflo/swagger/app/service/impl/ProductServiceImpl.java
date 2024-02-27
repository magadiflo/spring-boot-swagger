package dev.magadiflo.swagger.app.service.impl;

import dev.magadiflo.swagger.app.exception.ApiException;
import dev.magadiflo.swagger.app.persistence.entity.Product;
import dev.magadiflo.swagger.app.persistence.repository.IProductRepository;
import dev.magadiflo.swagger.app.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;

    @Override
    public List<Product> findAllProducts() {
        return this.productRepository.findAll();
    }

    @Override
    public Product findProductById(Long id) {
        return this.productRepository.findById(id)
                .orElseThrow(() -> new ApiException("No existe el producto con el id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public Product saveProduct(Product product) {
        return this.productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        return this.productRepository.findById(id)
                .map(productDB -> {
                    productDB.setName(product.getName());
                    productDB.setQuantityAvailable(product.getQuantityAvailable());
                    productDB.setPrice(product.getPrice());
                    productDB.setAvailable(product.getAvailable());
                    productDB.setCreationDate(product.getCreationDate());
                    return productDB;
                })
                .map(this.productRepository::save)
                .orElseThrow(() -> new ApiException("No existe el producto para actualizar con el id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteProductById(Long id) {
        this.productRepository.findById(id)
                .map(productDB -> {
                    this.productRepository.deleteById(productDB.getId());
                    return true;
                })
                .orElseThrow(() -> new ApiException("No existe el producto para eliminar con el id: " + id, HttpStatus.NOT_FOUND));
    }
}
