package com.easemanage.order.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.inventory.service.InventoryService;
import com.easemanage.notification.service.NotificationService;
import com.easemanage.order.dto.OrderItemRequest;
import com.easemanage.order.dto.PurchaseOrderRequest;
import com.easemanage.order.dto.PurchaseOrderResponse;
import com.easemanage.order.entity.*;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.supplier.entity.Supplier;
import com.easemanage.supplier.repository.SupplierRepository;
import com.easemanage.user.entity.Role;
import com.easemanage.user.entity.User;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    @Test
    void createPurchaseOrder_success() {
        Supplier supplier = buildSupplier(1L);
        Warehouse warehouse = buildWarehouse(1L);
        Product product = buildProduct(1L);
        User user = buildUser();

        OrderItemRequest itemReq = new OrderItemRequest(1L, 10, BigDecimal.valueOf(25));
        PurchaseOrderRequest request = new PurchaseOrderRequest(1L, 1L,
                LocalDate.now().plusDays(7), "Test notes", List.of(itemReq));

        PurchaseOrder saved = buildPurchaseOrder(1L, supplier, warehouse, product, user);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(saved);

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request, user);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        verify(auditService).log(eq("PurchaseOrder"), eq(1L), eq("CREATE"), isNull(), contains("orderNumber="));
    }

    @Test
    void updateStatus_toReceived_addsStockViaInventoryService() {
        Product product = buildProduct(1L);
        Supplier supplier = buildSupplier(1L);
        Warehouse warehouse = buildWarehouse(1L);

        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .id(1L).product(product).quantity(10)
                .unitCost(BigDecimal.TEN).receivedQuantity(0)
                .build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(supplier)
                .warehouse(warehouse)
                .status(OrderStatus.APPROVED)
                .totalAmount(BigDecimal.valueOf(100))
                .items(new ArrayList<>(List.of(item)))
                .build();
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        item.setPurchaseOrder(po);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        purchaseOrderService.updateStatus(1L, OrderStatus.RECEIVED);

        verify(inventoryService).addStock(eq(1L), eq(1L), eq(10));
        verify(auditService).log(eq("PurchaseOrder"), eq(1L), eq("UPDATE"),
                eq("status=APPROVED"), eq("status=RECEIVED"));
    }

    @Test
    void deletePurchaseOrder_onlyWorksForDraftStatus() {
        PurchaseOrder draftPo = PurchaseOrder.builder()
                .id(1L).orderNumber("PO-000001").status(OrderStatus.DRAFT)
                .supplier(buildSupplier(1L)).warehouse(buildWarehouse(1L))
                .totalAmount(BigDecimal.ZERO).items(new ArrayList<>())
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(draftPo));

        purchaseOrderService.deletePurchaseOrder(1L);
        verify(purchaseOrderRepository).delete(draftPo);
    }

    @Test
    void deletePurchaseOrder_nonDraft_throwsIllegalArgumentException() {
        PurchaseOrder approvedPo = PurchaseOrder.builder()
                .id(2L).orderNumber("PO-000002").status(OrderStatus.APPROVED)
                .supplier(buildSupplier(1L)).warehouse(buildWarehouse(1L))
                .totalAmount(BigDecimal.ZERO).items(new ArrayList<>())
                .build();

        when(purchaseOrderRepository.findById(2L)).thenReturn(Optional.of(approvedPo));

        assertThrows(IllegalArgumentException.class, () -> purchaseOrderService.deletePurchaseOrder(2L));
        verify(purchaseOrderRepository, never()).delete(any());
    }

    @Test
    void updateStatus_notToReceived_doesNotAddStock() {
        Supplier supplier = buildSupplier(1L);
        Warehouse warehouse = buildWarehouse(1L);

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L).orderNumber("PO-000001")
                .supplier(supplier).warehouse(warehouse)
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        purchaseOrderService.updateStatus(1L, OrderStatus.APPROVED);

        verify(inventoryService, never()).addStock(anyLong(), anyLong(), anyInt());
    }

    private Supplier buildSupplier(Long id) {
        Supplier s = Supplier.builder().id(id).name("Supplier " + id)
                .isActive(true).build();
        s.setCreatedAt(LocalDateTime.now());
        return s;
    }

    private Warehouse buildWarehouse(Long id) {
        Warehouse w = Warehouse.builder().id(id).name("Warehouse " + id)
                .code("WH" + id).isActive(true).build();
        w.setCreatedAt(LocalDateTime.now());
        return w;
    }

    private Product buildProduct(Long id) {
        return Product.builder().id(id).name("Product " + id).sku("SKU" + id)
                .costPrice(BigDecimal.TEN).sellingPrice(BigDecimal.valueOf(20))
                .reorderPoint(5).minStockLevel(2).build();
    }

    private User buildUser() {
        return User.builder().id(1L).username("admin").email("admin@test.com")
                .firstName("Admin").lastName("User").role(Role.ADMIN)
                .passwordHash("hash").build();
    }

    private PurchaseOrder buildPurchaseOrder(Long id, Supplier supplier, Warehouse warehouse,
                                              Product product, User user) {
        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .id(1L).product(product).quantity(10)
                .unitCost(BigDecimal.valueOf(25)).receivedQuantity(0).build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(id).orderNumber("PO-000001")
                .supplier(supplier).warehouse(warehouse)
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.valueOf(250))
                .items(new ArrayList<>(List.of(item)))
                .createdBy(user)
                .build();
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        item.setPurchaseOrder(po);
        return po;
    }
}
