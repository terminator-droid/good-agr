package org.example.repository;

import org.example.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Можно добавить userId, если понадобится
}