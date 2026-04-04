package com.easemanage.supplier.controller;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.supplier.dto.SupplierProductRequest;
import com.easemanage.supplier.dto.SupplierProductResponse;
import com.easemanage.supplier.dto.SupplierRequest;
import com.easemanage.supplier.dto.SupplierResponse;
import com.easemanage.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<PagedResponse<SupplierResponse>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(supplierService.getSuppliers(page, size, search));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupplierResponse>> getAllActive() {
        return ResponseEntity.ok(supplierService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id,
                                                            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<SupplierProductResponse>> getSupplierProducts(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierProducts(id));
    }

    @PostMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierProductResponse> addProductToSupplier(
            @PathVariable Long id, @Valid @RequestBody SupplierProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.addProductToSupplier(id, request));
    }

    @DeleteMapping("/{supplierId}/products/{spId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> removeProductFromSupplier(@PathVariable Long supplierId, @PathVariable Long spId) {
        supplierService.removeProductFromSupplier(supplierId, spId);
        return ResponseEntity.noContent().build();
    }
}
