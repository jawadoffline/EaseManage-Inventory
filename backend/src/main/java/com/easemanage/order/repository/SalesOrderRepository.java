package com.easemanage.order.repository;

import com.easemanage.order.entity.OrderStatus;
import com.easemanage.order.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    @Query("SELECT so FROM SalesOrder so JOIN FETCH so.warehouse WHERE " +
           "(:status IS NULL OR so.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(so.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(so.customerName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SalesOrder> search(@Param("status") OrderStatus status,
                            @Param("search") String search, Pageable pageable);

    long countByStatus(OrderStatus status);
}
