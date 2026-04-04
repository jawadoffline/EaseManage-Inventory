package com.easemanage.order.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.notification.service.NotificationService;
import com.easemanage.order.dto.OrderItemRequest;
import com.easemanage.order.dto.SalesOrderRequest;
import com.easemanage.order.dto.SalesOrderResponse;
import com.easemanage.order.entity.*;
import com.easemanage.order.repository.SalesOrderRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SalesOrderService salesOrderService;

    @Test
    void createSalesOrder_success() {
        Warehouse warehouse = buildWarehouse(1L);
        Product product = buildProduct(1L);
        User user = buildUser();

        OrderItemRequest itemReq = new OrderItemRequest(1L, 5, BigDecimal.valueOf(30));
        SalesOrderRequest request = new SalesOrderRequest("Customer A", 1L,
                "123 Shipping St", List.of(itemReq));

        SalesOrder saved = buildSalesOrder(1L, warehouse, product, user);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(saved);

        SalesOrderResponse response = salesOrderService.createSalesOrder(request, user);

        assertNotNull(response);
        assertEquals("PENDING", response.status());
        assertEquals("Customer A", response.customerName());
        verify(salesOrderRepository).save(any(SalesOrder.class));
        verify(auditService).log(eq("SalesOrder"), eq(1L), eq("CREATE"), isNull(), contains("orderNumber="));
    }

    @Test
    void updateStatus_changesStatus() {
        Warehouse warehouse = buildWarehouse(1L);

        SalesOrder so = SalesOrder.builder()
                .id(1L).orderNumber("SO-000001")
                .customerName("Customer A")
                .warehouse(warehouse)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(150))
                .items(new ArrayList<>())
                .build();
        so.setCreatedAt(LocalDateTime.now());
        so.setUpdatedAt(LocalDateTime.now());

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(so);

        SalesOrderResponse response = salesOrderService.updateStatus(1L, OrderStatus.APPROVED);

        verify(auditService).log(eq("SalesOrder"), eq(1L), eq("UPDATE"),
                eq("status=PENDING"), eq("status=APPROVED"));
        verify(notificationService).createNotification(eq(1L), eq("Sales Order Updated"),
                contains("SO-000001"), eq("ORDER_STATUS"));
    }

    @Test
    void deleteSalesOrder_pendingStatus_success() {
        SalesOrder pendingSo = SalesOrder.builder()
                .id(1L).orderNumber("SO-000001")
                .customerName("Customer")
                .warehouse(buildWarehouse(1L))
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(pendingSo));

        salesOrderService.deleteSalesOrder(1L);

        verify(salesOrderRepository).delete(pendingSo);
    }

    @Test
    void deleteSalesOrder_nonPending_throwsIllegalArgumentException() {
        SalesOrder approvedSo = SalesOrder.builder()
                .id(2L).orderNumber("SO-000002")
                .customerName("Customer")
                .warehouse(buildWarehouse(1L))
                .status(OrderStatus.APPROVED)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        when(salesOrderRepository.findById(2L)).thenReturn(Optional.of(approvedSo));

        assertThrows(IllegalArgumentException.class, () -> salesOrderService.deleteSalesOrder(2L));
        verify(salesOrderRepository, never()).delete(any());
    }

    @Test
    void getSalesOrderById_notFound_throwsException() {
        when(salesOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> salesOrderService.getSalesOrderById(99L));
    }

    private Warehouse buildWarehouse(Long id) {
        Warehouse w = Warehouse.builder().id(id).name("Warehouse " + id)
                .code("WH" + id).isActive(true).build();
        w.setCreatedAt(LocalDateTime.now());
        return w;
    }

    private Product buildProduct(Long id) {
        return Product.builder().id(id).name("Product " + id).sku("SKU" + id)
                .costPrice(BigDecimal.TEN).sellingPrice(BigDecimal.valueOf(30))
                .reorderPoint(5).minStockLevel(2).build();
    }

    private User buildUser() {
        return User.builder().id(1L).username("admin").email("admin@test.com")
                .firstName("Admin").lastName("User").role(Role.ADMIN)
                .passwordHash("hash").build();
    }

    private SalesOrder buildSalesOrder(Long id, Warehouse warehouse, Product product, User user) {
        SalesOrderItem item = SalesOrderItem.builder()
                .id(1L).product(product).quantity(5)
                .unitPrice(BigDecimal.valueOf(30)).build();

        SalesOrder so = SalesOrder.builder()
                .id(id).orderNumber("SO-000001")
                .customerName("Customer A")
                .warehouse(warehouse)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(150))
                .shippingAddress("123 Shipping St")
                .items(new ArrayList<>(List.of(item)))
                .createdBy(user)
                .build();
        so.setCreatedAt(LocalDateTime.now());
        so.setUpdatedAt(LocalDateTime.now());
        item.setSalesOrder(so);
        return so;
    }
}
