package com.harshith.E_Cart.Repo;

import com.harshith.E_Cart.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select distinct o
            from CustomerOrder o
            left join fetch o.items item
            left join fetch item.product
            join o.customer customer
            where o.id = :id and lower(customer.email) = lower(:email)
            """)
    Optional<Order> findByIdAndCustomerEmailWithItemsAndProducts(
            @Param("id") Long id,
            @Param("email") String email);
}
