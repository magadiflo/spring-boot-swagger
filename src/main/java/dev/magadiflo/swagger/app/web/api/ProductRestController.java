package dev.magadiflo.swagger.app.web.api;

import dev.magadiflo.swagger.app.persistence.entity.Product;
import dev.magadiflo.swagger.app.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Products", description = "API de gestión de productos")
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductRestController {

    private final IProductService productService;

    @Operation(tags = {"reading"})
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(this.productService.findAllProducts());
    }

    @Operation(
            summary = "Recupera un producto por su id",
            description = "Obtiene un objeto de Product especificando su id. " +
                    "La respuesta es un objeto Product con id, name, quantityAvailability " +
                    "price, availability y creationDate.",
            tags = {"reading"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Product.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "El product con el id dado no fue encontrado", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema())}),
    })
    @GetMapping(path = "/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(this.productService.findProductById(productId));
    }

    @Operation(tags = {"modification"})
    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
        Product productDB = this.productService.saveProduct(product);
        URI uri = URI.create("/api/v1/products/" + productDB.getId());
        return ResponseEntity.created(uri).body(productDB);
    }

    @Operation(tags = {"modification"})
    @PutMapping(path = "/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product product) {
        return ResponseEntity.ok(this.productService.updateProduct(productId, product));
    }

    @Operation(tags = {"modification"})
    @DeleteMapping(path = "/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        this.productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();
    }
}
