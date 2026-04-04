package com.easemanage.order.repository;

import com.easemanage.order.entity.OrderStatus;
import com.easemanage.order.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier JOIN FETCH po.warehouse WHERE " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(po.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseOrder> search(@Param("status") OrderStatus status,
                               @Param("search") String search, Pageable pageable);

    long countByStatus(OrderStatus status);
}
