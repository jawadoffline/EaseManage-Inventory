package com.easemanage.common.controller;

import com.easemanage.category.entity.Category;
import com.easemanage.category.repository.CategoryRepository;
import com.easemanage.customer.entity.Customer;
import com.easemanage.customer.repository.CustomerRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.supplier.entity.Supplier;
import com.easemanage.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class BulkImportController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private static final AtomicLong skuCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> importProducts(@RequestParam("file") MultipartFile file) {
        return processImport(file, lines -> {
            int imported = 0, skipped = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                try {
                    String[] cols = parseCsvLine(lines.get(i));
                    if (cols.length < 4) { skipped++; errors.add("Row " + (i+1) + ": insufficient columns"); continue; }

                    String name = cols[0].trim();
                    if (name.isEmpty() || productRepository.existsByName(name)) { skipped++; continue; }

                    Product product = Product.builder()
                        .sku("PRD-" + String.format("%06d", skuCounter.incrementAndGet()))
                        .name(name)
                        .description(cols.length > 1 ? cols[1].trim() : null)
                        .unitOfMeasure(cols.length > 2 && !cols[2].trim().isEmpty() ? cols[2].trim() : "PCS")
                        .costPrice(cols.length > 3 && !cols[3].trim().isEmpty() ? new BigDecimal(cols[3].trim()) : BigDecimal.ZERO)
                        .sellingPrice(cols.length > 4 && !cols[4].trim().isEmpty() ? new BigDecimal(cols[4].trim()) : BigDecimal.ZERO)
                        .minStockLevel(cols.length > 5 && !cols[5].trim().isEmpty() ? Integer.parseInt(cols[5].trim()) : 0)
                        .reorderPoint(cols.length > 6 && !cols[6].trim().isEmpty() ? Integer.parseInt(cols[6].trim()) : 0)
                        .barcode(cols.length > 7 ? cols[7].trim() : null)
                        .build();

                    // Try to match category by name
                    if (cols.length > 8 && !cols[8].trim().isEmpty()) {
                        categoryRepository.findAll().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(cols[8].trim()))
                            .findFirst()
                            .ifPresent(product::setCategory);
                    }

                    productRepository.save(product);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                    errors.add("Row " + (i+1) + ": " + e.getMessage());
                }
            }
            return Map.of("imported", imported, "skipped", skipped, "errors", errors);
        });
    }

    @PostMapping("/suppliers")
    public ResponseEntity<Map<String, Object>> importSuppliers(@RequestParam("file") MultipartFile file) {
        return processImport(file, lines -> {
            int imported = 0, skipped = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                try {
                    String[] cols = parseCsvLine(lines.get(i));
                    if (cols.length < 1 || cols[0].trim().isEmpty()) { skipped++; continue; }

                    Supplier supplier = Supplier.builder()
                        .name(cols[0].trim())
                        .email(cols.length > 1 ? cols[1].trim() : null)
                        .phone(cols.length > 2 ? cols[2].trim() : null)
                        .contactPerson(cols.length > 3 ? cols[3].trim() : null)
                        .city(cols.length > 4 ? cols[4].trim() : null)
                        .country(cols.length > 5 ? cols[5].trim() : null)
                        .paymentTerms(cols.length > 6 ? cols[6].trim() : null)
                        .build();

                    supplierRepository.save(supplier);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                    errors.add("Row " + (i+1) + ": " + e.getMessage());
                }
            }
            return Map.of("imported", imported, "skipped", skipped, "errors", errors);
        });
    }

    @PostMapping("/customers")
    public ResponseEntity<Map<String, Object>> importCustomers(@RequestParam("file") MultipartFile file) {
        return processImport(file, lines -> {
            int imported = 0, skipped = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                try {
                    String[] cols = parseCsvLine(lines.get(i));
                    if (cols.length < 1 || cols[0].trim().isEmpty()) { skipped++; continue; }

                    Customer customer = Customer.builder()
                        .name(cols[0].trim())
                        .email(cols.length > 1 ? cols[1].trim() : null)
                        .phone(cols.length > 2 ? cols[2].trim() : null)
                        .contactPerson(cols.length > 3 ? cols[3].trim() : null)
                        .city(cols.length > 4 ? cols[4].trim() : null)
                        .country(cols.length > 5 ? cols[5].trim() : null)
                        .build();

                    customerRepository.save(customer);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                    errors.add("Row " + (i+1) + ": " + e.getMessage());
                }
            }
            return Map.of("imported", imported, "skipped", skipped, "errors", errors);
        });
    }

    private ResponseEntity<Map<String, Object>> processImport(MultipartFile file,
            java.util.function.Function<List<String>, Map<String, Object>> processor) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) lines.add(line);
                }
            }
            if (lines.size() < 2) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must have a header row and at least one data row"));
            }
            return ResponseEntity.ok(processor.apply(lines));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { result.add(current.toString()); current = new StringBuilder(); }
            else { current.append(c); }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
