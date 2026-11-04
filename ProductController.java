
package com.harshith.E_Cart.Controller;

import com.harshith.E_Cart.DTO.ProductDto;
import com.harshith.E_Cart.Service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Get all products
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                productService.getAllProducts(page, size)
        );
    }

    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable long id) {

        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    // Filter by category
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> filterByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                productService.filterProducts(
                        category, null, null, null,
                        null, null, page, size
                )
        );
    }

    // Filter by price
    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> filterByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                productService.filterProducts(
                        null, minPrice, maxPrice, null,
                        null, null, page, size
                )
        );
    }

    // Search by keyword
    @GetMapping("/keyword")
    public ResponseEntity<Map<String, Object>> filterByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                productService.filterProducts(
                        null, null, null, keyword,
                        null, null, page, size
                )
        );
    }

    // Filter by rating
    @GetMapping("/rating")
    public ResponseEntity<Map<String, Object>> filterByRating(
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                productService.filterProducts(
                        null, null, null, null,
                        minRating, maxRating, page, size
                )
        );
    }

    // Combined filters
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                productService.filterProducts(
                        category, minPrice, maxPrice,
                        keyword, minRating, maxRating,
                        page, size
                )
        );
    }

}
