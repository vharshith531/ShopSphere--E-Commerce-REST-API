package com.harshith.E_Cart.DTO;

public class ProductReviewResponseDto {

    private Long id;
    private long productId;
    private Integer rating;
    private String comment;

    public ProductReviewResponseDto(
            Long id, long productId, Integer rating, String comment) {
        this.id = id;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public long getProductId() {
        return productId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
