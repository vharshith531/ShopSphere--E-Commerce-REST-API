package com.harshith.E_Cart.DTO;

import com.harshith.E_Cart.Entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponseDto {

    private final Long orderId;
    private final Instant orderDate;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final List<OrderItemResponseDto> items;

    public OrderResponseDto(
            Long orderId,
            Instant orderDate,
            OrderStatus status,
            BigDecimal totalAmount,
            List<OrderItemResponseDto> items) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = List.copyOf(items);
    }

    public Long getOrderId() {
        return orderId;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemResponseDto> getItems() {
        return items;
    }
}
