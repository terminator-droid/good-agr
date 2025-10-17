package org.example.http.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.Cart;
import org.example.entity.Product;
import org.example.service.CartService;
import org.example.service.CartService.BasketOptimizationResult;
import org.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping("/{cartId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCart(cartId));
    }

    @PostMapping("/{cartId}/add")
    public ResponseEntity<Cart> addProduct(@PathVariable Long cartId, @RequestParam Long productId, @RequestParam int quantity) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(cartService.addProduct(cartId, product, quantity));
    }

    @GetMapping("/{cartId}/optimize")
    public ResponseEntity<BasketOptimizationResult> optimizeCart(@PathVariable Long cartId) {
        Cart cart = cartService.getCart(cartId);
        return ResponseEntity.ok(cartService.optimizeCart(cart));
    }
}
