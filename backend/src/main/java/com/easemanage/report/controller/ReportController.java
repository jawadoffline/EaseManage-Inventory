package com.easemanage.report.controller;

import com.easemanage.report.dto.*;
import com.easemanage.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/inventory-valuation")
    public ResponseEntity<InventoryValuationReport> getInventoryValuation() {
        return ResponseEntity.ok(reportService.getInventoryValuation());
    }

    @GetMapping("/stock-summary")
    public ResponseEntity<StockSummaryReport> getStockSummary() {
        return ResponseEntity.ok(reportService.getStockSummary());
    }

    @GetMapping("/order-summary")
    public ResponseEntity<OrderSummaryReport> getOrderSummary() {
        return ResponseEntity.ok(reportService.getOrderSummary());
    }
}
