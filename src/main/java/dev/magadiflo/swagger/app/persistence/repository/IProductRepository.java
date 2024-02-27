package dev.magadiflo.swagger.app.persistence.repository;

import dev.magadiflo.swagger.app.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProductRepository extends JpaRepository<Product, Long> {
}
