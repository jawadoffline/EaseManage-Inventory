package com.easemanage.product.controller;

import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductExportController {

    private final ProductRepository productRepository;

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<Product> products = productRepository.findAll(Sort.by("name"));
        StringBuilder csv = new StringBuilder();
        csv.append("SKU,Name,Description,Category,Unit,Min Stock,Max Stock,Reorder Point,Cost Price,Selling Price,Barcode,Active\n");
        for (Product p : products) {
            csv.append(escapeCsv(p.getSku())).append(",");
            csv.append(escapeCsv(p.getName())).append(",");
            csv.append(escapeCsv(p.getDescription())).append(",");
            csv.append(escapeCsv(p.getCategory() != null ? p.getCategory().getName() : "")).append(",");
            csv.append(escapeCsv(p.getUnitOfMeasure())).append(",");
            csv.append(p.getMinStockLevel()).append(",");
            csv.append(p.getMaxStockLevel() != null ? p.getMaxStockLevel() : "").append(",");
            csv.append(p.getReorderPoint()).append(",");
            csv.append(p.getCostPrice()).append(",");
            csv.append(p.getSellingPrice()).append(",");
            csv.append(escapeCsv(p.getBarcode())).append(",");
            csv.append(p.getIsActive()).append("\n");
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.toString().getBytes());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
