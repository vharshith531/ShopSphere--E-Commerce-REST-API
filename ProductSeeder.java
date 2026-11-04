
package com.harshith.E_Cart.Seed;

import com.harshith.E_Cart.Entity.Product;
import com.harshith.E_Cart.Entity.ProductImage;
import com.harshith.E_Cart.Repo.ProductImageRepository;
import com.harshith.E_Cart.Repo.ProductRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductSeeder(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository) {

        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    private static final Map<String, String> IMAGE_PATHS = Map.ofEntries(
            Map.entry("Apple iPhone 16",
                    "/product_images/apple-iphone-16.png"),

            Map.entry("Samsung Galaxy S25",
                    "/product_images/samsung-galaxy-s25.png"),

            Map.entry("OnePlus 13",
                    "/product_images/oneplus-13.png"),

            Map.entry("Sony WH-1000XM5",
                    "/product_images/sony-wh-1000xm5.png"),

            Map.entry("Apple MacBook Air M3",
                    "/product_images/macbook-air-m3.png"),

            Map.entry("Dell Inspiron 15",
                    "/product_images/dell-inspiron-15.png"),

            Map.entry("HP Pavilion Keyboard",
                    "/product_images/hp-pavilion-keyboard.png"),

            Map.entry("Logitech MX Master 3S",
                    "/product_images/logitech-mx-master-3s.png"),

            Map.entry("Samsung 55-inch Smart TV",
                    "/product_images/samsung-55-smart-tv.png"),

            Map.entry("Apple AirPods Pro",
                    "/product_images/apple-airpods-pro.png"),

            Map.entry("Lenovo IdeaPad Slim 5",
                    "/product_images/lenovo-ideapad-slim-5.png"),

            Map.entry("Canon EOS R50 Camera",
                    "/product_images/canon-eos-r50.png"),

            Map.entry("Apple iPad Air",
                    "/product_images/apple-ipad-air.png"),

            Map.entry("JBL Flip 6",
                    "/product_images/jbl-flip-6.png")
    );

    @Override
    @Transactional
    public void run(String... args) {

        if (productRepository.count() == 0) {

            List<Product> demoProducts = List.of(

                    new Product(0L, "Apple iPhone 16", 799.00,
                            "Smartphone with advanced camera", 4.5,
                            "Amazon", 50, 120, "Smartphones",
                            List.of(IMAGE_PATHS.get("Apple iPhone 16"))),

                    new Product(0L, "Samsung Galaxy S25", 899.00,
                            "Android smartphone with AMOLED display", 4.6,
                            "Samsung", 40, 95, "Smartphones",
                            List.of(IMAGE_PATHS.get("Samsung Galaxy S25"))),

                    new Product(0L, "OnePlus 13", 749.00,
                            "Flagship smartphone with fast charging", 4.4,
                            "OnePlus", 35, 80, "Smartphones",
                            List.of(IMAGE_PATHS.get("OnePlus 13"))),

                    new Product(0L, "Sony WH-1000XM5", 349.00,
                            "Wireless noise cancelling headphones", 4.7,
                            "Sony", 25, 200, "Headphones",
                            List.of(IMAGE_PATHS.get("Sony WH-1000XM5"))),

                    new Product(0L, "Apple MacBook Air M3", 1099.00,
                            "Laptop with Apple M3 chip", 4.8,
                            "Apple", 15, 75, "Laptops",
                            List.of(IMAGE_PATHS.get("Apple MacBook Air M3"))),

                    new Product(0L, "Dell Inspiron 15", 699.00,
                            "Laptop for work and students", 4.2,
                            "Dell", 20, 60, "Laptops",
                            List.of(IMAGE_PATHS.get("Dell Inspiron 15"))),

                    new Product(0L, "HP Pavilion Keyboard", 49.00,
                            "Wireless computer keyboard", 4.1,
                            "HP", 60, 45, "Accessories",
                            List.of(IMAGE_PATHS.get("HP Pavilion Keyboard"))),

                    new Product(0L, "Logitech MX Master 3S", 99.00,
                            "Wireless ergonomic mouse", 4.6,
                            "Logitech", 30, 150, "Accessories",
                            List.of(IMAGE_PATHS.get("Logitech MX Master 3S"))),

                    new Product(0L, "Samsung 55-inch Smart TV", 599.00,
                            "4K Ultra HD Smart LED television", 4.3,
                            "Samsung", 10, 90, "Televisions",
                            List.of(IMAGE_PATHS.get("Samsung 55-inch Smart TV"))),

                    new Product(0L, "Apple AirPods Pro", 249.00,
                            "Wireless earbuds with active noise cancellation", 4.7,
                            "Apple", 45, 300, "Audio",
                            List.of(IMAGE_PATHS.get("Apple AirPods Pro"))),

                    new Product(0L, "Lenovo IdeaPad Slim 5", 799.00,
                            "Lightweight laptop for productivity", 4.3,
                            "Lenovo", 25, 65, "Laptops",
                            List.of(IMAGE_PATHS.get("Lenovo IdeaPad Slim 5"))),

                    new Product(0L, "Canon EOS R50 Camera", 679.00,
                            "Mirrorless camera for photography and video", 4.6,
                            "Canon", 20, 85, "Cameras",
                            List.of(IMAGE_PATHS.get("Canon EOS R50 Camera"))),

                    new Product(0L, "Apple iPad Air", 599.00,
                            "Tablet for entertainment, study and productivity", 4.7,
                            "Apple", 30, 140, "Tablets",
                            List.of(IMAGE_PATHS.get("Apple iPad Air"))),

                    new Product(0L, "JBL Flip 6", 129.00,
                            "Portable Bluetooth speaker", 4.5,
                            "JBL", 40, 110, "Audio",
                            List.of(IMAGE_PATHS.get("JBL Flip 6")))
            );

            productRepository.saveAll(demoProducts);

            System.out.println("14 demo products inserted successfully!");

        } else {

            // Update image paths for existing products
            List<ProductImage> images = productImageRepository.findAll();

            for (ProductImage image : images) {

                if (image.getProduct() == null) {
                    continue;
                }

                String productName = image.getProduct().getName();

                String correctPath = IMAGE_PATHS.get(productName);

                if (correctPath != null) {
                    image.setImageUrl(correctPath);
                }
            }

            productImageRepository.saveAll(images);

            System.out.println("Existing product image paths updated successfully!");
        }
    }
}
