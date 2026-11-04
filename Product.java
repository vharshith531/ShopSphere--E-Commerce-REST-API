
package com.harshith.E_Cart.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false)
    @PositiveOrZero(message = "Price must be zero or greater")
    private double price;

    @NotBlank(message = "Description is required")
    private String description;

    private String category;

    private double rating = 0.0;

    @NotBlank(message = "Seller is required")
    private String seller;

    @NotNull(message = "Stock cannot be null")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock = 0;

    @PositiveOrZero(message = "Number of reviews cannot be negative")
    private Integer numOfReviews = 0;

    // Product images relationship
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductImage> images = new ArrayList<>();

    // Product reviews relationship
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductReview> reviews = new ArrayList<>();

    public Product() {
    }

    public Product(long id, String name, double price,
                   String description, double rating,
                   String seller, Integer stock,
                   Integer numOfReviews, String category,
                   List<String> images) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.rating = rating;
        this.seller = seller;
        this.stock = stock;
        this.numOfReviews = numOfReviews;

        this.images = new ArrayList<>();

        if (images != null) {
            images.forEach(this::addImage);
        }
    }

    // Maintain both sides of the relationship
    public void addImage(String imageUrl) {
        ProductImage productImage = new ProductImage(imageUrl, this);
        this.images.add(productImage);
    }

    public void addReview(ProductReview review) {
        review.setProduct(this);
        this.reviews.add(review);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getNumOfReviews() {
        return numOfReviews;
    }

    public void setNumOfReviews(Integer numOfReviews) {
        this.numOfReviews = numOfReviews;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = new ArrayList<>();

        if (images != null) {
            for (ProductImage image : images) {
                image.setProduct(this);
                this.images.add(image);
            }
        }
    }

    public List<ProductReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<ProductReview> reviews) {
        this.reviews = new ArrayList<>();
        if (reviews != null) {
            reviews.forEach(this::addReview);
        }
    }
}
