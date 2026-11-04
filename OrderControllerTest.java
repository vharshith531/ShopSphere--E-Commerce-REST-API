package com.harshith.E_Cart.Controller;

import com.harshith.E_Cart.DTO.OrderItemResponseDto;
import com.harshith.E_Cart.DTO.OrderResponseDto;
import com.harshith.E_Cart.Entity.OrderStatus;
import com.harshith.E_Cart.Exception.GlobalExceptionHandler;
import com.harshith.E_Cart.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private OrderService orderService;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("buyer@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt));
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new OrderController(orderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsCreatedOrderDto() throws Exception {
        when(orderService.createOrder(
                any(),
                eq("buyer@example.com")
        )).thenReturn(new OrderResponseDto(
                41L,
                Instant.parse("2026-11-04T12:00:00Z"),
                OrderStatus.PLACED,
                new BigDecimal("24.50"),
                List.of(new OrderItemResponseDto(
                        3L,
                        "Headphones",
                        1,
                        new BigDecimal("24.50"),
                        new BigDecimal("24.50")
                ))
        ));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":3,"quantity":1}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(41))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(24.50))
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Headphones"));
    }

    @Test
    void returnsBadRequestForInvalidOrderItem() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":3,"quantity":0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").exists());

        verifyNoInteractions(orderService);
    }

    @Test
    void returnsOrderDetailsById() throws Exception {
        when(orderService.getOrder(41L, "buyer@example.com"))
                .thenReturn(new OrderResponseDto(
                41L,
                Instant.parse("2026-11-04T12:00:00Z"),
                OrderStatus.PLACED,
                new BigDecimal("24.50"),
                List.of(new OrderItemResponseDto(
                        3L,
                        "Headphones",
                        1,
                        new BigDecimal("24.50"),
                        new BigDecimal("24.50")
                ))
        ));

        mockMvc.perform(get("/api/orders/41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(41))
                .andExpect(jsonPath("$.items[0].productId").value(3))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[0].subtotal").value(24.50));
    }

    @Test
    void returnsNotFoundForUnknownOrder() throws Exception {
        when(orderService.getOrder(404L, "buyer@example.com")).thenThrow(
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: 404"
                )
        );

        mockMvc.perform(get("/api/orders/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Order not found with ID: 404"));
    }
}
