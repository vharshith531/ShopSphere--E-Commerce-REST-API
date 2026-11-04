package com.harshith.E_Cart.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class OrderRequestDto {

    @NotEmpty(message = "At least one order item is required")
    @Size(max = 100, message = "An order cannot contain more than 100 items")
    private List<@NotNull(message = "Order item cannot be null")
    @Valid OrderItemRequestDto> items;

    public OrderRequestDto() {
    }

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }
}
