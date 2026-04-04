package com.easemanage.stockmovement.repository;

import com.easemanage.stockmovement.entity.MovementType;
import com.easemanage.stockmovement.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT sm FROM StockMovement sm " +
           "JOIN FETCH sm.product p " +
           "LEFT JOIN FETCH sm.fromWarehouse " +
           "LEFT JOIN FETCH sm.toWarehouse " +
           "LEFT JOIN FETCH sm.createdBy " +
           "WHERE (:productId IS NULL OR p.id = :productId) " +
           "AND (:warehouseId IS NULL OR sm.fromWarehouse.id = :warehouseId OR sm.toWarehouse.id = :warehouseId) " +
           "AND (:movementType IS NULL OR sm.movementType = :movementType)")
    Page<StockMovement> search(@Param("productId") Long productId,
                               @Param("warehouseId") Long warehouseId,
                               @Param("movementType") MovementType movementType,
                               Pageable pageable);
}
