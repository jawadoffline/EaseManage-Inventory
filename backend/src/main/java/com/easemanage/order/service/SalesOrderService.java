package com.easemanage.order.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.notification.service.NotificationService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.order.dto.*;
import com.easemanage.order.entity.*;
import com.easemanage.order.repository.SalesOrderRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.user.entity.User;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private static final AtomicLong soCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional(readOnly = true)
    public PagedResponse<SalesOrderResponse> getSalesOrders(int page, int size, OrderStatus status, String search) {
        Page<SalesOrder> orders = salesOrderRepository.search(status, search,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
            orders.getContent().stream().map(this::toResponse).toList(),
            orders.getNumber(), orders.getSize(),
            orders.getTotalElements(), orders.getTotalPages(), orders.isLast()
        );
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getSalesOrderById(Long id) {
        return toResponse(findById(id));
    }

    public SalesOrderResponse createSalesOrder(SalesOrderRequest request, User currentUser) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));

        SalesOrder so = SalesOrder.builder()
            .orderNumber("SO-" + String.format("%06d", soCounter.incrementAndGet()))
            .customerName(request.customerName())
            .warehouse(warehouse)
            .status(OrderStatus.PENDING)
            .shippingAddress(request.shippingAddress())
            .createdBy(currentUser)
            .build();

        for (OrderItemRequest item : request.items()) {
            Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));
            so.getItems().add(SalesOrderItem.builder()
                .salesOrder(so).product(product)
                .quantity(item.quantity()).unitPrice(item.unitPrice())
                .build());
        }
        so.recalculateTotal();
        SalesOrder saved = salesOrderRepository.save(so);
        auditService.log("SalesOrder", saved.getId(), "CREATE", null, "orderNumber=" + saved.getOrderNumber());
        return toResponse(saved);
    }

    public SalesOrderResponse updateStatus(Long id, OrderStatus newStatus) {
        SalesOrder so = findById(id);
        OrderStatus oldStatus = so.getStatus();
        so.setStatus(newStatus);
        SalesOrder updated = salesOrderRepository.save(so);
        auditService.log("SalesOrder", id, "UPDATE", "status=" + oldStatus, "status=" + newStatus);

        notificationService.createNotification(1L, "Sales Order Updated",
            "SO " + so.getOrderNumber() + " status changed to " + newStatus, "ORDER_STATUS");

        return toResponse(updated);
    }

    public void deleteSalesOrder(Long id) {
        SalesOrder so = findById(id);
        if (so.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Can only delete orders in PENDING status");
        }
        salesOrderRepository.delete(so);
    }

    private SalesOrder findById(Long id) {
        return salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order", id));
    }

    private SalesOrderResponse toResponse(SalesOrder so) {
        return new SalesOrderResponse(
            so.getId(), so.getOrderNumber(), so.getCustomerName(),
            so.getWarehouse().getId(), so.getWarehouse().getName(),
            so.getStatus().name(), so.getTotalAmount(),
            so.getShippingAddress(),
            so.getCreatedBy() != null ? so.getCreatedBy().getFirstName() + " " + so.getCreatedBy().getLastName() : null,
            so.getItems().stream().map(i -> new PurchaseOrderResponse.OrderItemResponse(
                i.getId(), i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                i.getQuantity(), i.getUnitPrice(), 0
            )).toList(),
            so.getCreatedAt(), so.getUpdatedAt()
        );
    }
}
