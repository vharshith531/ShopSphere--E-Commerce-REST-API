package com.harshith.E_Cart.Controller;

import com.harshith.E_Cart.DTO.ProductReviewDto;
import com.harshith.E_Cart.DTO.ProductReviewRequestDto;
import com.harshith.E_Cart.DTO.ProductReviewResponseDto;
import com.harshith.E_Cart.Service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductReviewController {

    private final ProductService productService;

    public ProductReviewController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewResponseDto> addProductReview(
            @PathVariable long productId,
            @Valid @RequestBody ProductReviewRequestDto reviewDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addReview(productId, reviewDto));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<ProductReviewResponseDto>> getProductReviews(
            @PathVariable long productId) {
        return ResponseEntity.ok(productService.getReviews(productId));
    }

    @PostMapping("/review")
    public ResponseEntity<ProductReviewDto> addReview(
            @Valid @RequestBody ProductReviewDto reviewDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addReview(reviewDto));
    }
}
