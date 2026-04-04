package com.easemanage.report.service;

import com.easemanage.category.entity.Category;
import com.easemanage.inventory.entity.Inventory;
import com.easemanage.inventory.repository.InventoryRepository;
import com.easemanage.order.entity.OrderStatus;
import com.easemanage.order.entity.PurchaseOrder;
import com.easemanage.order.entity.SalesOrder;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.order.repository.SalesOrderRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.report.dto.InventoryValuationReport;
import com.easemanage.report.dto.OrderSummaryReport;
import com.easemanage.report.dto.StockSummaryReport;
import com.easemanage.warehouse.entity.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getInventoryValuation_returnsCorrectTotals() {
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product product = Product.builder()
                .id(1L).name("Widget").sku("SKU001")
                .category(category)
                .costPrice(BigDecimal.valueOf(10))
                .sellingPrice(BigDecimal.valueOf(25))
                .reorderPoint(5).minStockLevel(2)
                .build();

        when(productRepository.findAll(any(Sort.class))).thenReturn(List.of(product));
        when(inventoryRepository.getTotalStockByProductId(1L)).thenReturn(100);

        InventoryValuationReport report = reportService.getInventoryValuation();

        assertEquals(BigDecimal.valueOf(1000), report.totalCostValue());
        assertEquals(BigDecimal.valueOf(2500), report.totalRetailValue());
        assertEquals(1, report.totalProducts());
        assertEquals(100, report.totalUnits());
        assertEquals(1, report.items().size());
    }

    @Test
    void getStockSummary_countsCorrectly() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        Product inStockProduct = Product.builder()
                .id(1L).name("In Stock").sku("SKU1")
                .category(category).reorderPoint(5).build();
        Product lowStockProduct = Product.builder()
                .id(2L).name("Low Stock").sku("SKU2")
                .category(category).reorderPoint(20).build();
        Product outOfStockProduct = Product.builder()
                .id(3L).name("Out of Stock").sku("SKU3")
                .category(category).reorderPoint(5).build();

        Warehouse warehouse = Warehouse.builder().id(1L).name("WH").code("WH1").build();

        Inventory inv1 = Inventory.builder()
                .id(1L).product(inStockProduct).warehouse(warehouse).quantity(50).reservedQuantity(0).build();
        Inventory inv2 = Inventory.builder()
                .id(2L).product(lowStockProduct).warehouse(warehouse).quantity(10).reservedQuantity(0).build();

        when(inventoryRepository.findAll()).thenReturn(List.of(inv1, inv2));
        when(productRepository.findAll()).thenReturn(List.of(inStockProduct, lowStockProduct, outOfStockProduct));

        StockSummaryReport report = reportService.getStockSummary();

        assertEquals(3, report.totalProducts());
        assertEquals(1, report.inStockCount());
        assertEquals(1, report.lowStockCount());
        assertEquals(1, report.outOfStockCount());
    }

    @Test
    void getOrderSummary_returnsStatusCounts() {
        Warehouse warehouse = Warehouse.builder().id(1L).name("WH").code("WH1").build();
        var supplier = com.easemanage.supplier.entity.Supplier.builder()
                .id(1L).name("Supplier").isActive(true).build();

        PurchaseOrder po1 = PurchaseOrder.builder()
                .id(1L).orderNumber("PO-1").supplier(supplier).warehouse(warehouse)
                .status(OrderStatus.DRAFT).totalAmount(BigDecimal.valueOf(100))
                .items(new ArrayList<>()).build();
        PurchaseOrder po2 = PurchaseOrder.builder()
                .id(2L).orderNumber("PO-2").supplier(supplier).warehouse(warehouse)
                .status(OrderStatus.DRAFT).totalAmount(BigDecimal.valueOf(200))
                .items(new ArrayList<>()).build();

        SalesOrder so1 = SalesOrder.builder()
                .id(1L).orderNumber("SO-1").customerName("Cust")
                .warehouse(warehouse).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(50)).items(new ArrayList<>()).build();

        when(purchaseOrderRepository.findAll()).thenReturn(List.of(po1, po2));
        when(salesOrderRepository.findAll()).thenReturn(List.of(so1));

        OrderSummaryReport report = reportService.getOrderSummary();

        assertEquals(2, report.totalPurchaseOrders());
        assertEquals(BigDecimal.valueOf(300), report.totalPurchaseValue());
        assertEquals(2, report.purchaseByStatus().get("DRAFT"));
        assertEquals(1, report.totalSalesOrders());
        assertEquals(BigDecimal.valueOf(50), report.totalSalesValue());
        assertEquals(1, report.salesByStatus().get("PENDING"));
    }

    @Test
    void getInventoryValuation_noStock_returnsZeros() {
        Product product = Product.builder()
                .id(1L).name("Empty").sku("SKU001")
                .costPrice(BigDecimal.valueOf(10))
                .sellingPrice(BigDecimal.valueOf(25))
                .reorderPoint(5).minStockLevel(2)
                .build();

        when(productRepository.findAll(any(Sort.class))).thenReturn(List.of(product));
        when(inventoryRepository.getTotalStockByProductId(1L)).thenReturn(null);

        InventoryValuationReport report = reportService.getInventoryValuation();

        assertEquals(BigDecimal.ZERO, report.totalCostValue());
        assertEquals(BigDecimal.ZERO, report.totalRetailValue());
        assertEquals(0, report.totalUnits());
    }
}
