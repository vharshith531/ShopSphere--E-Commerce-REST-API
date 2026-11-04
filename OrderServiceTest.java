package com.harshith.E_Cart.Service;

import com.harshith.E_Cart.DTO.OrderItemRequestDto;
import com.harshith.E_Cart.DTO.OrderRequestDto;
import com.harshith.E_Cart.DTO.OrderResponseDto;
import com.harshith.E_Cart.Entity.Order;
import com.harshith.E_Cart.Entity.CustomerAccount;
import com.harshith.E_Cart.Entity.Product;
import com.harshith.E_Cart.Repo.CustomerAccountRepository;
import com.harshith.E_Cart.Repo.OrderRepository;
import com.harshith.E_Cart.Repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private CustomerAccountRepository accountRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        accountRepository = mock(CustomerAccountRepository.class);
        orderService = new OrderService(
                orderRepository,
                productRepository,
                accountRepository
        );
        when(accountRepository.findByEmailIgnoreCase("buyer@example.com"))
                .thenReturn(Optional.of(
                        new CustomerAccount("buyer@example.com", "hash")
                ));
    }

    @Test
    void createsOrderFromDatabasePricesAndDecrementsStock() {
        Product product = product(1L, "Phone", 12.345, 10);
        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.createOrder(
                request(item(1L, 2), item(1L, 1)),
                "buyer@example.com"
        );

        assertEquals(7, product.getStock());
        assertEquals(0, response.getTotalAmount().compareTo(
                new java.math.BigDecimal("37.05")
        ));
        assertEquals(1, response.getItems().size());
        assertEquals(3, response.getItems().get(0).getQuantity());
        assertEquals("Phone", response.getItems().get(0).getProductName());
        assertEquals(
                0,
                response.getItems().get(0).getUnitPrice()
                        .compareTo(new java.math.BigDecimal("12.35"))
        );
        assertNotNull(response.getOrderDate());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void doesNotChangeAnyStockWhenAProductIsUnavailable() {
        Product firstProduct = product(1L, "Phone", 12.00, 10);
        Product secondProduct = product(2L, "Tablet", 24.00, 1);
        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(firstProduct));
        when(productRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(secondProduct));

        assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(
                        request(item(1L, 2), item(2L, 2)),
                        "buyer@example.com"
                )
        );

        assertEquals(10, firstProduct.getStock());
        assertEquals(1, secondProduct.getStock());
        verify(orderRepository, never()).save(any(Order.class));
    }

    private Product product(long id, String name, double price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }

    private OrderRequestDto request(OrderItemRequestDto... items) {
        OrderRequestDto request = new OrderRequestDto();
        request.setItems(List.of(items));
        return request;
    }

    private OrderItemRequestDto item(long productId, int quantity) {
        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }
}
