package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.*;
import org.example.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    private static final BigDecimal SAMOKAT_DELIVERY_FEE = new BigDecimal("99.00");
    private static final BigDecimal LAVKA_DELIVERY_FEE = new BigDecimal("199.00");

    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @Transactional
    public Cart addProduct(Long cartId, Product product, int quantity) {
        Cart cart = getCart(cartId);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem item = CartItem.builder().cart(cart).product(product).quantity(quantity).build();
            cart.getItems().add(item);
        }
        return cartRepository.save(cart);
    }

    // --- ОПТИМИЗАЦИЯ КОРЗИНЫ ---

    public BasketOptimizationResult optimizeCart(Cart cart) {
        BigDecimal totalSamokat = BigDecimal.ZERO;
        BigDecimal totalLavka = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal sPrice = (product.getShop() == Shop.SAMOKAT) ? product.getCurrentPrice() : null;
            BigDecimal lPrice = (product.getShop() == Shop.LAVKA) ? product.getCurrentPrice() : null;

            if (sPrice != null) {
                totalSamokat = totalSamokat.add(sPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            if (lPrice != null) {
                totalLavka = totalLavka.add(lPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        totalSamokat = totalSamokat.add(SAMOKAT_DELIVERY_FEE);
        totalLavka = totalLavka.add(LAVKA_DELIVERY_FEE);

        boolean samokatIsBetter = totalSamokat.compareTo(totalLavka) <= 0;

        return BasketOptimizationResult.builder()
                .totalSamokat(totalSamokat)
                .totalLavka(totalLavka)
                .recommendedShop(samokatIsBetter ? Shop.SAMOKAT : Shop.LAVKA)
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class BasketOptimizationResult {
        private BigDecimal totalSamokat;
        private BigDecimal totalLavka;
        private Shop recommendedShop;
    }
}
