package com.easemanage.order.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.inventory.service.InventoryService;
import com.easemanage.notification.service.NotificationService;
import com.easemanage.order.dto.*;
import com.easemanage.order.entity.*;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.supplier.entity.Supplier;
import com.easemanage.supplier.repository.SupplierRepository;
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
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private static final AtomicLong poCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional(readOnly = true)
    public PagedResponse<PurchaseOrderResponse> getPurchaseOrders(int page, int size, OrderStatus status, String search) {
        Page<PurchaseOrder> orders = purchaseOrderRepository.search(status, search,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
            orders.getContent().stream().map(this::toResponse).toList(),
            orders.getNumber(), orders.getSize(),
            orders.getTotalElements(), orders.getTotalPages(), orders.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        return toResponse(findById(id));
    }

    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request, User currentUser) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));

        PurchaseOrder po = PurchaseOrder.builder()
            .orderNumber("PO-" + String.format("%06d", poCounter.incrementAndGet()))
            .supplier(supplier).warehouse(warehouse)
            .status(OrderStatus.DRAFT)
            .expectedDelivery(request.expectedDelivery())
            .notes(request.notes())
            .createdBy(currentUser)
            .build();

        for (OrderItemRequest item : request.items()) {
            Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));
            po.getItems().add(PurchaseOrderItem.builder()
                .purchaseOrder(po).product(product)
                .quantity(item.quantity()).unitCost(item.unitPrice())
                .build());
        }
        po.recalculateTotal();
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.log("PurchaseOrder", saved.getId(), "CREATE", null, "orderNumber=" + saved.getOrderNumber());
        return toResponse(saved);
    }

    public PurchaseOrderResponse updateStatus(Long id, OrderStatus newStatus) {
        PurchaseOrder po = findById(id);
        OrderStatus oldStatus = po.getStatus();

        if (newStatus == OrderStatus.RECEIVED && oldStatus != OrderStatus.RECEIVED) {
            for (PurchaseOrderItem item : po.getItems()) {
                item.setReceivedQuantity(item.getQuantity());
                inventoryService.addStock(item.getProduct().getId(), po.getWarehouse().getId(), item.getQuantity());
            }
        }

        po.setStatus(newStatus);
        PurchaseOrder updated = purchaseOrderRepository.save(po);
        auditService.log("PurchaseOrder", id, "UPDATE", "status=" + oldStatus, "status=" + newStatus);

        notificationService.createNotification(1L, "Purchase Order Updated",
            "PO " + po.getOrderNumber() + " status changed to " + newStatus, "ORDER_STATUS");

        return toResponse(updated);
    }

    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = findById(id);
        if (po.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalArgumentException("Can only delete orders in DRAFT status");
        }
        purchaseOrderRepository.delete(po);
    }

    private PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        return new PurchaseOrderResponse(
            po.getId(), po.getOrderNumber(),
            po.getSupplier().getId(), po.getSupplier().getName(),
            po.getWarehouse().getId(), po.getWarehouse().getName(),
            po.getStatus().name(), po.getTotalAmount(),
            po.getExpectedDelivery(), po.getNotes(),
            po.getCreatedBy() != null ? po.getCreatedBy().getFirstName() + " " + po.getCreatedBy().getLastName() : null,
            po.getItems().stream().map(i -> new PurchaseOrderResponse.OrderItemResponse(
                i.getId(), i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                i.getQuantity(), i.getUnitCost(), i.getReceivedQuantity()
            )).toList(),
            po.getCreatedAt(), po.getUpdatedAt()
        );
    }
}
