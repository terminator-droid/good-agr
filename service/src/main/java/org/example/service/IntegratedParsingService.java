package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.entity.Shop.LAVKA;
import static org.example.entity.Shop.SAMOKAT;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegratedParsingService {

    private final LavkaParserService lavkaParserService;
    private final SamokatParserService samokatParserService;
    private final ProductRepository productRepository;

    /**
     * Основной метод парсинга всех магазинов
     */
    @Transactional
    public void parseAllShops() {
        log.info("Starting integrated parsing of all shops");

        try {
            List<Product> allProducts = new ArrayList<>();

            // Парсим Лавку
//            log.info("Parsing Lavka...");
//            List<Product> lavkaProducts = lavkaParserService.getProducts();
//            allProducts.addAll(lavkaProducts);
//            log.info("Lavka parsing completed: {} products", lavkaProducts.size());

            // Парсим Самокат
            log.info("Parsing Samokat...");
            List<Product> samokatProducts = samokatParserService.getProducts();
            allProducts.addAll(samokatProducts);
            log.info("Samokat parsing completed: {} products", samokatProducts.size());

            // Сохраняем все продукты
            saveProducts(allProducts);

            log.info("Integrated parsing completed successfully. Total products: {}", allProducts.size());

        } catch (InterruptedException e) {
            log.error("Parsing was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error during parsing", e);
        }
    }

    /**
     * Асинхронный парсинг Лавки
     */
    @Async
    public void parseLavkaAsync() {
        try {
            log.info("Starting async Lavka parsing");
            List<Product> products = lavkaParserService.getProducts();
            saveProducts(products);
            log.info("Async Lavka parsing completed: {} products", products.size());
        } catch (Exception e) {
            log.error("Error in async Lavka parsing", e);
        }
    }

    /**
     * Асинхронный парсинг Самоката
     */
    @Async
    public void parseSamokatAsync() {
        try {
            log.info("Starting async Samokat parsing");
            List<Product> products = samokatParserService.getProducts();
            saveProducts(products);
            log.info("Async Samokat parsing completed: {} products", products.size());
        } catch (Exception e) {
            log.error("Error in async Samokat parsing", e);
        }
    }

    /**
     * Запланированный парсинг каждые 2 часа
     */
    @Scheduled(fixedRate = 7200000) // 2 часа
    public void scheduledParsing() {
        log.info("Starting scheduled parsing");
        parseAllShops();
    }

    /**
     * Сохранение продуктов с проверкой на дубликаты
     */
    @Transactional
    protected void saveProducts(List<Product> products) {
        int savedCount = 0;
        int updatedCount = 0;

        for (Product product : products) {
            try {
                // Ищем существующий продукт по ссылке
                Optional<Product> existingProduct = productRepository.findByRef(product.getRef());

                if (existingProduct.isPresent()) {
                    // Обновляем существующий продукт
                    Product existing = existingProduct.get();
                    existing.setTitle(product.getTitle());
                    existing.setOldPriceStr(product.getOldPriceStr());
                    existing.setNewPriceStr(product.getNewPriceStr());
                    existing.setVolume(product.getVolume());
                    existing.setShop(product.getShop());
                    existing.setUpdatedAt(LocalDateTime.now());

                    productRepository.save(existing);
                    updatedCount++;
                } else {
                    // Сохраняем новый продукт
                    productRepository.save(product);
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("Error saving product: {}", product.getTitle(), e);
            }
        }

        log.info("Products processed: {} new, {} updated", savedCount, updatedCount);
    }

    /**
     * Получение статистики парсинга
     */
    public ParsingStats getParsingStats() {
        long totalProducts = productRepository.count();
        long lavkaProducts = productRepository.countByShop(LAVKA);
        long samokatProducts = productRepository.countByShop(SAMOKAT);

        return ParsingStats.builder()
                .totalProducts(totalProducts)
                .lavkaProducts(lavkaProducts)
                .samokatProducts(samokatProducts)
                .lastUpdate(productRepository.findTopByOrderByUpdatedAtDesc()
                        .map(Product::getUpdatedAt)
                        .orElse(null))
                .build();
    }

    /**
     * Статистика парсинга
     */
    @lombok.Data
    @lombok.Builder
    public static class ParsingStats {
        private long totalProducts;
        private long lavkaProducts;
        private long samokatProducts;
        private LocalDateTime lastUpdate;
    }
}
