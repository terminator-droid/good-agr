package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Product;
import org.example.entity.Shop;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Получение всех продуктов с кэшированием
     */
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByTitleAsc();
    }

    /**
     * Получение продуктов по магазину
     */
    @Cacheable(value = "products", key = "#shop.name()")
    public List<Product> getProductsByShop(Shop shop) {
        return productRepository.findByShopOrderByTitleAsc(shop);
    }

    /**
     * Поиск продуктов по названию
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findByTitleContainingIgnoreCaseOrderByTitleAsc(query.trim());
    }

    /**
     * Получение продуктов с пагинацией
     */
    public Page<Product> getProductsWithPagination(Pageable pageable) {
        List<Product> allProducts = getAllProducts();
        Sort sort = pageable.getSort();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProducts.size());

        List<Product> pageContent = allProducts.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allProducts.size());
    }

    /**
     * Получение продукта по ID
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    /**
     * Получение продуктов для сравнения цен
     * Группирует похожие товары из разных магазинов
     */
    public List<ProductComparison> getProductsForComparison() {
        List<Product> allProducts = productRepository.findProductsWithPrices();

        // Группируем по похожим названиям
        Map<String, List<Product>> groupedProducts = allProducts.stream()
                .collect(Collectors.groupingBy(this::normalizeProductName));

        return groupedProducts.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1) // Только товары из нескольких магазинов
                .map(entry -> createProductComparison(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProductComparison::getProductName))
                .collect(Collectors.toList());
    }

    /**
     * Нормализация названия продукта для группировки
     */
    private String normalizeProductName(Product product) {
        return product.getTitle()
                .toLowerCase()
                .replaceAll("[^a-zа-я0-9\s]", "")
                .replaceAll("\s+", " ")
                .trim();
    }

    /**
     * Создание объекта сравнения продуктов
     */
    private ProductComparison createProductComparison(String normalizedName, List<Product> products) {
        Product lavkaProduct = products.stream()
                .filter(p -> p.getShop() == Shop.LAVKA)
                .findFirst()
                .orElse(null);

        Product samokatProduct = products.stream()
                .filter(p -> p.getShop() == Shop.SAMOKAT)
                .findFirst()
                .orElse(null);

        if (lavkaProduct == null || samokatProduct == null) {
            return null;
        }

        BigDecimal lavkaPrice = lavkaProduct.getCurrentPrice();
        BigDecimal samokatPrice = samokatProduct.getCurrentPrice();

        if (lavkaPrice == null || samokatPrice == null) {
            return null;
        }

        // Определяем более дешевый вариант
        String cheaperShop;
        BigDecimal priceDifference;

        if (lavkaPrice.compareTo(samokatPrice) <= 0) {
            cheaperShop = "LAVKA";
            priceDifference = samokatPrice.subtract(lavkaPrice);
        } else {
            cheaperShop = "SAMOKAT";
            priceDifference = lavkaPrice.subtract(samokatPrice);
        }

        return ProductComparison.builder()
                .productName(lavkaProduct.getTitle())
                .lavkaProduct(lavkaProduct)
                .samokatProduct(samokatProduct)
                .lavkaPrice(lavkaPrice)
                .samokatPrice(samokatPrice)
                .cheaperShop(cheaperShop)
                .priceDifference(priceDifference)
                .build();
    }

    /**
     * Получение статистики продуктов
     */
    public ProductStats getProductStats() {
        long totalProducts = productRepository.count();
        long uniqueProducts = productRepository.countUniqueProducts();
        long lavkaProducts = productRepository.countByShop(Shop.LAVKA);
        long samokatProducts = productRepository.countByShop(Shop.SAMOKAT);

        return ProductStats.builder()
                .totalProducts(totalProducts)
                .uniqueProducts(uniqueProducts)
                .lavkaProducts(lavkaProducts)
                .samokatProducts(samokatProducts)
                .build();
    }

    /**
     * Класс для сравнения продуктов
     */
    @lombok.Data
    @lombok.Builder
    public static class ProductComparison {
        private String productName;
        private Product lavkaProduct;
        private Product samokatProduct;
        private BigDecimal lavkaPrice;
        private BigDecimal samokatPrice;
        private String cheaperShop;
        private BigDecimal priceDifference;
    }

    /**
     * Статистика продуктов
     */
    @lombok.Data
    @lombok.Builder
    public static class ProductStats {
        private long totalProducts;
        private long uniqueProducts;
        private long lavkaProducts;
        private long samokatProducts;
    }
}
