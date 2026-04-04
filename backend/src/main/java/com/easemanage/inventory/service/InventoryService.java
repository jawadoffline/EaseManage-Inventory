package com.easemanage.inventory.service;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.inventory.dto.InventoryAdjustRequest;
import com.easemanage.inventory.dto.InventoryResponse;
import com.easemanage.inventory.entity.Inventory;
import com.easemanage.inventory.repository.InventoryRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.notification.service.NotificationService;
import com.easemanage.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getInventory(int page, int size, Long warehouseId, String search) {
        Page<Inventory> inventory = inventoryRepository.search(warehouseId, search,
            PageRequest.of(page, size, Sort.by("product.name")));
        return new PagedResponse<>(
            inventory.getContent().stream().map(this::toResponse).toList(),
            inventory.getNumber(), inventory.getSize(),
            inventory.getTotalElements(), inventory.getTotalPages(), inventory.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock() {
        return inventoryRepository.findLowStock().stream().map(this::toResponse).toList();
    }

    public InventoryResponse adjustStock(InventoryAdjustRequest request) {
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));

        Inventory inventory = inventoryRepository
            .findByProductIdAndWarehouseId(request.productId(), request.warehouseId())
            .orElseGet(() -> Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(0)
                .reservedQuantity(0)
                .build());

        inventory.setQuantity(request.quantity());
        inventory.setLastCountedAt(LocalDateTime.now());
        InventoryResponse response = toResponse(inventoryRepository.save(inventory));

        if (inventory.getQuantity() <= product.getReorderPoint()) {
            notificationService.createNotification(1L, "Low Stock Alert",
                "Product " + product.getName() + " is low on stock (" + inventory.getQuantity() + " units remaining)",
                "LOW_STOCK");
        }

        return response;
    }

    public InventoryResponse addStock(Long productId, Long warehouseId, int quantityToAdd) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        Inventory inventory = inventoryRepository
            .findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseGet(() -> Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(0)
                .reservedQuantity(0)
                .build());

        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventory.setLastCountedAt(LocalDateTime.now());
        InventoryResponse response = toResponse(inventoryRepository.save(inventory));

        if (inventory.getQuantity() <= product.getReorderPoint()) {
            notificationService.createNotification(1L, "Low Stock Alert",
                "Product " + product.getName() + " is low on stock (" + inventory.getQuantity() + " units remaining)",
                "LOW_STOCK");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public long countLowStock() {
        return inventoryRepository.findLowStock().size();
    }

    private InventoryResponse toResponse(Inventory i) {
        Product p = i.getProduct();
        Warehouse w = i.getWarehouse();
        return new InventoryResponse(
            i.getId(), p.getId(), p.getName(), p.getSku(),
            w.getId(), w.getName(), w.getCode(),
            i.getQuantity(), i.getReservedQuantity(), i.getAvailableQuantity(),
            p.getMinStockLevel(), p.getReorderPoint(),
            i.getQuantity() <= p.getReorderPoint(),
            i.getLastCountedAt()
        );
    }
}
