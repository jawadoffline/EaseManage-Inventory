package com.easemanage.warehouse.repository;

import com.easemanage.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCode(String code);

    @Query("SELECT w FROM Warehouse w WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> search(@Param("search") String search, Pageable pageable);

    long countByIsActiveTrue();
}
