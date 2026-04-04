package com.easemanage.inventory.service;

import com.easemanage.inventory.dto.InventoryAdjustRequest;
import com.easemanage.inventory.dto.InventoryResponse;
import com.easemanage.inventory.entity.Inventory;
import com.easemanage.inventory.repository.InventoryRepository;
import com.easemanage.notification.service.NotificationService;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void adjustStock_createsNewInventoryEntry() {
        Product product = buildProduct(1L, "Widget", "SKU001", 10);
        Warehouse warehouse = buildWarehouse(1L);
        InventoryAdjustRequest request = new InventoryAdjustRequest(1L, 1L, 50, "Initial stock");

        Inventory saved = buildInventory(1L, product, warehouse, 50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);

        InventoryResponse response = inventoryService.adjustStock(request);

        assertEquals(50, response.quantity());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void adjustStock_updatesExistingEntry() {
        Product product = buildProduct(1L, "Widget", "SKU001", 10);
        Warehouse warehouse = buildWarehouse(1L);
        Inventory existing = buildInventory(1L, product, warehouse, 30);
        InventoryAdjustRequest request = new InventoryAdjustRequest(1L, 1L, 75, "Recount");

        Inventory saved = buildInventory(1L, product, warehouse, 75);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);

        InventoryResponse response = inventoryService.adjustStock(request);

        assertEquals(75, response.quantity());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void adjustStock_triggersLowStockNotification() {
        Product product = buildProduct(1L, "Widget", "SKU001", 20);
        Warehouse warehouse = buildWarehouse(1L);
        InventoryAdjustRequest request = new InventoryAdjustRequest(1L, 1L, 5, "Low stock");

        Inventory saved = buildInventory(1L, product, warehouse, 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);

        inventoryService.adjustStock(request);

        verify(notificationService).createNotification(eq(1L), eq("Low Stock Alert"),
                contains("Widget"), eq("LOW_STOCK"));
    }

    @Test
    void addStock_incrementsQuantity() {
        Product product = buildProduct(1L, "Widget", "SKU001", 10);
        Warehouse warehouse = buildWarehouse(1L);
        Inventory existing = buildInventory(1L, product, warehouse, 20);
        Inventory saved = buildInventory(1L, product, warehouse, 30);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);

        InventoryResponse response = inventoryService.addStock(1L, 1L, 10);

        assertEquals(30, response.quantity());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void getLowStock_returnsCorrectItems() {
        Product product = buildProduct(1L, "Widget", "SKU001", 50);
        Warehouse warehouse = buildWarehouse(1L);
        Inventory lowStockItem = buildInventory(1L, product, warehouse, 5);

        when(inventoryRepository.findLowStock()).thenReturn(List.of(lowStockItem));

        List<InventoryResponse> result = inventoryService.getLowStock();

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).quantity());
        assertTrue(result.get(0).lowStock());
    }

    @Test
    void addStock_createsNewEntryWhenNotExists() {
        Product product = buildProduct(1L, "Widget", "SKU001", 10);
        Warehouse warehouse = buildWarehouse(1L);
        Inventory saved = buildInventory(1L, product, warehouse, 25);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);

        InventoryResponse response = inventoryService.addStock(1L, 1L, 25);

        assertNotNull(response);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    private Product buildProduct(Long id, String name, String sku, int reorderPoint) {
        return Product.builder()
                .id(id)
                .name(name)
                .sku(sku)
                .reorderPoint(reorderPoint)
                .minStockLevel(5)
                .costPrice(BigDecimal.TEN)
                .sellingPrice(BigDecimal.valueOf(20))
                .build();
    }

    private Warehouse buildWarehouse(Long id) {
        Warehouse w = Warehouse.builder()
                .id(id)
                .name("Warehouse " + id)
                .code("WH" + id)
                .isActive(true)
                .build();
        w.setCreatedAt(LocalDateTime.now());
        return w;
    }

    private Inventory buildInventory(Long id, Product product, Warehouse warehouse, int quantity) {
        return Inventory.builder()
                .id(id)
                .product(product)
                .warehouse(warehouse)
                .quantity(quantity)
                .reservedQuantity(0)
                .lastCountedAt(LocalDateTime.now())
                .build();
    }
}
