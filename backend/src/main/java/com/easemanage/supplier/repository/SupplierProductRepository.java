package com.easemanage.supplier.repository;

import com.easemanage.supplier.entity.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {
    List<SupplierProduct> findBySupplierId(Long supplierId);
    List<SupplierProduct> findByProductId(Long productId);
    boolean existsBySupplierIdAndProductId(Long supplierId, Long productId);
}
