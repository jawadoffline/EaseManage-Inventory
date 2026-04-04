package com.easemanage.order.controller;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.order.dto.SalesOrderRequest;
import com.easemanage.order.dto.SalesOrderResponse;
import com.easemanage.order.entity.OrderStatus;
import com.easemanage.order.service.SalesOrderService;
import com.easemanage.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public ResponseEntity<PagedResponse<SalesOrderResponse>> getSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(salesOrderService.getSalesOrders(page, size, status, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> getSalesOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(
            @Valid @RequestBody SalesOrderRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesOrderService.createSalesOrder(request, user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_STAFF')")
    public ResponseEntity<SalesOrderResponse> updateStatus(@PathVariable Long id,
                                                            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(salesOrderService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.noContent().build();
    }
}
