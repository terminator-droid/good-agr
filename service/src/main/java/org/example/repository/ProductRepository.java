package org.example.repository;

import org.example.entity.Product;
import org.example.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Поиск по ссылке (уникальный идентификатор из парсеров)
    Optional<Product> findByRef(String ref);

    // Поиск по магазину
    List<Product> findByShop(Shop shop);
    List<Product> findByShopOrderByTitleAsc(Shop shop);

    // Подсчет по магазинам
    long countByShop(Shop shop);

    // Поиск по названию (игнорируя регистр)
    List<Product> findByTitleContainingIgnoreCaseOrderByTitleAsc(String title);

    // Все продукты, отсортированные по названию
    List<Product> findAllByOrderByTitleAsc();

    // Поиск по объему
    List<Product> findByVolumeContainingIgnoreCase(String volume);

    // Последние обновленные продукты
    List<Product> findTop10ByOrderByUpdatedAtDesc();
    Optional<Product> findTopByOrderByUpdatedAtDesc();

    // Продукты с ценами (исключаем товары без цен)
    @Query("SELECT p FROM Product p WHERE p.newPrice IS NOT NULL OR p.oldPrice IS NOT NULL ORDER BY p.title")
    List<Product> findProductsWithPrices();

    // Поиск дубликатов по названию и объему
    @Query("SELECT p FROM Product p WHERE p.title = :title AND p.volume = :volume")
    List<Product> findDuplicatesByTitleAndVolume(@Param("title") String title, @Param("volume") String volume);

    // Статистические запросы
    @Query("SELECT COUNT(DISTINCT p.title) FROM Product p")
    long countUniqueProducts();

    @Query("SELECT p.volume, COUNT(p) FROM Product p GROUP BY p.volume ORDER BY COUNT(p) DESC")
    List<Object[]> getVolumeStatistics();
}
