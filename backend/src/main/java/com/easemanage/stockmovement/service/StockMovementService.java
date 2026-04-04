package com.easemanage.stockmovement.service;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.stockmovement.dto.StockMovementRequest;
import com.easemanage.stockmovement.dto.StockMovementResponse;
import com.easemanage.stockmovement.entity.MovementType;
import com.easemanage.stockmovement.entity.StockMovement;
import com.easemanage.stockmovement.repository.StockMovementRepository;
import com.easemanage.user.entity.User;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public PagedResponse<StockMovementResponse> getMovements(int page, int size,
                                                              Long productId, Long warehouseId,
                                                              String movementType) {
        MovementType type = movementType != null ? MovementType.valueOf(movementType) : null;
        Page<StockMovement> movements = stockMovementRepository.search(productId, warehouseId, type,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
            movements.getContent().stream().map(this::toResponse).toList(),
            movements.getNumber(), movements.getSize(),
            movements.getTotalElements(), movements.getTotalPages(), movements.isLast()
        );
    }

    public StockMovementResponse recordMovement(StockMovementRequest request, User currentUser) {
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        Warehouse fromWarehouse = null;
        if (request.fromWarehouseId() != null) {
            fromWarehouse = warehouseRepository.findById(request.fromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.fromWarehouseId()));
        }

        Warehouse toWarehouse = null;
        if (request.toWarehouseId() != null) {
            toWarehouse = warehouseRepository.findById(request.toWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.toWarehouseId()));
        }

        StockMovement movement = StockMovement.builder()
            .product(product)
            .fromWarehouse(fromWarehouse)
            .toWarehouse(toWarehouse)
            .quantity(request.quantity())
            .movementType(MovementType.valueOf(request.movementType()))
            .reason(request.reason())
            .createdBy(currentUser)
            .build();

        return toResponse(stockMovementRepository.save(movement));
    }

    private StockMovementResponse toResponse(StockMovement sm) {
        Product p = sm.getProduct();
        Warehouse from = sm.getFromWarehouse();
        Warehouse to = sm.getToWarehouse();
        User user = sm.getCreatedBy();
        return new StockMovementResponse(
            sm.getId(),
            p.getId(), p.getName(), p.getSku(),
            from != null ? from.getId() : null,
            from != null ? from.getName() : null,
            to != null ? to.getId() : null,
            to != null ? to.getName() : null,
            sm.getQuantity(),
            sm.getMovementType().name(),
            sm.getReferenceType(),
            sm.getReferenceId(),
            sm.getReason(),
            user != null ? user.getId() : null,
            user != null ? user.getFirstName() + " " + user.getLastName() : null,
            sm.getCreatedAt()
        );
    }
}
