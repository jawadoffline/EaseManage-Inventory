package com.easemanage.common.controller;

import com.easemanage.category.repository.CategoryRepository;
import com.easemanage.common.dto.ChartData;
import com.easemanage.common.dto.DashboardStats;
import com.easemanage.inventory.entity.Inventory;
import com.easemanage.inventory.repository.InventoryRepository;
import com.easemanage.inventory.service.InventoryService;
import com.easemanage.order.entity.OrderStatus;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.order.repository.SalesOrderRepository;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(new DashboardStats(
            productRepository.countByIsActiveTrue(),
            inventoryService.countLowStock(),
            warehouseRepository.countByIsActiveTrue(),
            categoryRepository.count()
        ));
    }

    @GetMapping("/charts")
    public ResponseEntity<ChartData> getChartData() {
        // Stock by category: group inventory by product's category, sum quantities
        List<Inventory> allInventory = inventoryRepository.findAll();
        Map<String, Integer> stockByCategory = allInventory.stream()
            .collect(Collectors.groupingBy(
                inv -> {
                    var category = inv.getProduct().getCategory();
                    return category != null ? category.getName() : "Uncategorized";
                },
                Collectors.summingInt(Inventory::getQuantity)
            ));

        List<ChartData.CategoryStockData> categoryStockData = stockByCategory.entrySet().stream()
            .map(e -> new ChartData.CategoryStockData(e.getKey(), e.getValue()))
            .toList();

        // Order status: count purchase + sales orders combined by status
        List<ChartData.OrderStatusData> orderStatusData = Arrays.stream(OrderStatus.values())
            .map(status -> {
                long count = purchaseOrderRepository.countByStatus(status)
                           + salesOrderRepository.countByStatus(status);
                return new ChartData.OrderStatusData(status.name(), (int) count);
            })
            .filter(d -> d.value() > 0)
            .toList();

        return ResponseEntity.ok(new ChartData(categoryStockData, orderStatusData));
    }
}
