package com.easemanage.common.controller;

import com.easemanage.product.repository.ProductRepository;
import com.easemanage.supplier.repository.SupplierRepository;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.order.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;

    @GetMapping
    public ResponseEntity<Map<String, List<Map<String, Object>>>> search(@RequestParam String q) {
        Map<String, List<Map<String, Object>>> results = new LinkedHashMap<>();

        // Products
        List<Map<String, Object>> products = new ArrayList<>();
        productRepository.search(q, null, null, PageRequest.of(0, 5)).getContent()
            .forEach(p -> products.add(Map.of(
                "id", p.getId(), "title", p.getName(),
                "description", "SKU: " + p.getSku() + (p.getCategory() != null ? " | " + p.getCategory().getName() : ""),
                "url", "/products"
            )));
        if (!products.isEmpty()) results.put("Products", products);

        // Suppliers
        List<Map<String, Object>> suppliers = new ArrayList<>();
        supplierRepository.search(q, PageRequest.of(0, 5)).getContent()
            .forEach(s -> suppliers.add(Map.of(
                "id", s.getId(), "title", s.getName(),
                "description", (s.getContactPerson() != null ? s.getContactPerson() : "") + (s.getCity() != null ? " | " + s.getCity() : ""),
                "url", "/suppliers"
            )));
        if (!suppliers.isEmpty()) results.put("Suppliers", suppliers);

        // Purchase Orders
        List<Map<String, Object>> pos = new ArrayList<>();
        purchaseOrderRepository.search(null, q, PageRequest.of(0, 5)).getContent()
            .forEach(po -> pos.add(Map.of(
                "id", po.getId(), "title", po.getOrderNumber(),
                "description", po.getSupplier().getName() + " | " + po.getStatus().name(),
                "url", "/purchase-orders"
            )));
        if (!pos.isEmpty()) results.put("Purchase Orders", pos);

        // Sales Orders
        List<Map<String, Object>> sos = new ArrayList<>();
        salesOrderRepository.search(null, q, PageRequest.of(0, 5)).getContent()
            .forEach(so -> sos.add(Map.of(
                "id", so.getId(), "title", so.getOrderNumber(),
                "description", so.getCustomerName() + " | " + so.getStatus().name(),
                "url", "/sales-orders"
            )));
        if (!sos.isEmpty()) results.put("Sales Orders", sos);

        return ResponseEntity.ok(results);
    }
}
