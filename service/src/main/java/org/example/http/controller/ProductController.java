package org.example.http.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.Product;
import org.example.service.ProductService;
import org.example.service.ProductService.ProductComparison;
import org.example.service.ProductService.ProductStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    /**
     * Получить все продукты (отсортированные по title).
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Получить продукты с пагинацией.
     * Пример: /api/products/pageable?page=0&size=20
     */
    @GetMapping("/pageable")
    public ResponseEntity<Page<Product>> getProductsWithPagination(Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsWithPagination(pageable));
    }

    /**
     * Получить продукт по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Поиск продуктов по названию (GET /api/products/search?query=молоко).
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    /**
     * Получить статистику по количеству товаров.
     */
    @GetMapping("/stats")
    public ResponseEntity<ProductStats> getProductStats() {
        return ResponseEntity.ok(productService.getProductStats());
    }

    /**
     * Получить сопоставление товаров для сравнения цен (только похожие из обоих магазинов).
     */
    @GetMapping("/comparison")
    public ResponseEntity<List<ProductComparison>> getProductsForComparison() {
        return ResponseEntity.ok(productService.getProductsForComparison());
    }
}
