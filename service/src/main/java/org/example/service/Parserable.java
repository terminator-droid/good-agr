package org.example.service;

import org.example.entity.Product;

import java.util.List;

public interface Parserable {
    List<Product> getProducts() throws InterruptedException;
}