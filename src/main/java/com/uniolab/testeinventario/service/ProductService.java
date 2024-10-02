package com.uniolab.testeinventario.service;

import com.uniolab.testeinventario.model.Product;
import com.uniolab.testeinventario.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(Product productOne) {
        return productRepository.save(productOne);
    }
}
