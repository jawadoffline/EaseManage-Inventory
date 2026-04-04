package com.easemanage.order.controller;

import com.easemanage.order.service.OrderPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderPdfController {

    private final OrderPdfService orderPdfService;

    @GetMapping("/purchase-orders/{id}/pdf")
    public ResponseEntity<byte[]> getPurchaseOrderPdf(@PathVariable Long id) {
        byte[] pdf = orderPdfService.generatePurchaseOrderPdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PO-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/sales-orders/{id}/pdf")
    public ResponseEntity<byte[]> getSalesOrderPdf(@PathVariable Long id) {
        byte[] pdf = orderPdfService.generateSalesOrderPdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SO-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
