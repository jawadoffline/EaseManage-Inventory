package com.easemanage.stockmovement.controller;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.stockmovement.dto.StockMovementRequest;
import com.easemanage.stockmovement.dto.StockMovementResponse;
import com.easemanage.stockmovement.service.StockMovementService;
import com.easemanage.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<PagedResponse<StockMovementResponse>> getMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String movementType) {
        return ResponseEntity.ok(stockMovementService.getMovements(page, size, productId, warehouseId, movementType));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_STAFF')")
    public ResponseEntity<StockMovementResponse> recordMovement(
            @Valid @RequestBody StockMovementRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(stockMovementService.recordMovement(request, user));
    }
}
