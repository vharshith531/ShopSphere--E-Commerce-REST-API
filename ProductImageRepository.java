package com.harshith.E_Cart.Repo;

import com.harshith.E_Cart.Entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {
}
