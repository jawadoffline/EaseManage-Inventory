package com.easemanage.inventory.controller;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.inventory.dto.InventoryAdjustRequest;
import com.easemanage.inventory.dto.InventoryResponse;
import com.easemanage.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<PagedResponse<InventoryResponse>> getInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inventoryService.getInventory(page, size, warehouseId, search));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStock());
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_STAFF')")
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(request));
    }
}
