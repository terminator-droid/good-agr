package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "old_price")
    private String oldPriceStr;  // Храним как строку из парсера

    @Column(name = "new_price")
    private String newPriceStr;  // Храним как строку из парсера

    @Column(name = "old_price_decimal", precision = 10, scale = 2)
    private BigDecimal oldPrice; // Преобразованная цена для расчетов

    @Column(name = "new_price_decimal", precision = 10, scale = 2)
    private BigDecimal newPrice; // Преобразованная цена для расчетов

    private String volume;

    @Column(name = "ref", unique = true)
    private String ref; // URL ссылка на товар

    @Enumerated(EnumType.STRING)
    private Shop shop;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        // Автоматически преобразуем строковые цены в BigDecimal
        convertPrices();
    }

    private void convertPrices() {
        if (oldPriceStr != null && !oldPriceStr.isEmpty()) {
            oldPrice = parsePrice(oldPriceStr);
        }
        if (newPriceStr != null && !newPriceStr.isEmpty()) {
            newPrice = parsePrice(newPriceStr);
        }
    }

    private BigDecimal parsePrice(String priceStr) {
        try {
            // Убираем все символы кроме цифр и точки/запятой
            String cleanPrice = priceStr.replaceAll("[^0-9.,]", "")
                    .replace(",", ".");
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Метод для получения актуальной цены
    public BigDecimal getCurrentPrice() {
        return newPrice != null ? newPrice : oldPrice;
    }

    // Для совместимости с вашим кодом
    public String getOldPrice() {
        return oldPriceStr;
    }

    public String getNewPrice() {
        return newPriceStr;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPriceStr = oldPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPriceStr = newPrice;
    }
}