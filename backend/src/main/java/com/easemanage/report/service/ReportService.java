package com.easemanage.report.service;

import com.easemanage.inventory.entity.Inventory;
import com.easemanage.inventory.repository.InventoryRepository;
import com.easemanage.order.entity.OrderStatus;
import com.easemanage.order.entity.PurchaseOrder;
import com.easemanage.order.entity.SalesOrder;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.order.repository.SalesOrderRepository;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;

    public InventoryValuationReport getInventoryValuation() {
        List<Product> products = productRepository.findAll(Sort.by("name"));
        List<InventoryValuationReport.ProductValuation> items = new ArrayList<>();

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalRetail = BigDecimal.ZERO;
        int totalUnits = 0;

        for (Product p : products) {
            int qty = Optional.ofNullable(inventoryRepository.getTotalStockByProductId(p.getId())).orElse(0);
            BigDecimal costVal = p.getCostPrice().multiply(BigDecimal.valueOf(qty));
            BigDecimal retailVal = p.getSellingPrice().multiply(BigDecimal.valueOf(qty));
            totalCost = totalCost.add(costVal);
            totalRetail = totalRetail.add(retailVal);
            totalUnits += qty;

            items.add(new InventoryValuationReport.ProductValuation(
                p.getId(), p.getName(), p.getSku(),
                p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                qty, p.getCostPrice(), p.getSellingPrice(), costVal, retailVal
            ));
        }

        return new InventoryValuationReport(totalCost, totalRetail, products.size(), totalUnits, items);
    }

    public StockSummaryReport getStockSummary() {
        List<Inventory> allInventory = inventoryRepository.findAll();
        Map<Long, Integer> productStock = new HashMap<>();
        for (Inventory inv : allInventory) {
            productStock.merge(inv.getProduct().getId(), inv.getQuantity(), Integer::sum);
        }

        List<Product> products = productRepository.findAll();
        int inStock = 0, lowStock = 0, outOfStock = 0;
        Map<String, int[]> categoryMap = new LinkedHashMap<>();

        for (Product p : products) {
            int qty = productStock.getOrDefault(p.getId(), 0);
            String cat = p.getCategory() != null ? p.getCategory().getName() : "Uncategorized";
            categoryMap.computeIfAbsent(cat, k -> new int[2]);
            categoryMap.get(cat)[0]++;
            categoryMap.get(cat)[1] += qty;

            if (qty == 0) outOfStock++;
            else if (qty <= p.getReorderPoint()) lowStock++;
            else inStock++;
        }

        List<StockSummaryReport.CategoryStock> byCategory = categoryMap.entrySet().stream()
            .map(e -> new StockSummaryReport.CategoryStock(e.getKey(), e.getValue()[0], e.getValue()[1]))
            .toList();

        return new StockSummaryReport(products.size(), inStock, lowStock, outOfStock, byCategory);
    }

    public OrderSummaryReport getOrderSummary() {
        List<PurchaseOrder> pos = purchaseOrderRepository.findAll();
        BigDecimal totalPurchaseValue = pos.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Integer> poByStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            long count = pos.stream().filter(po -> po.getStatus() == s).count();
            if (count > 0) poByStatus.put(s.name(), (int) count);
        }

        List<SalesOrder> sos = salesOrderRepository.findAll();
        BigDecimal totalSalesValue = sos.stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Integer> soByStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            long count = sos.stream().filter(so -> so.getStatus() == s).count();
            if (count > 0) soByStatus.put(s.name(), (int) count);
        }

        return new OrderSummaryReport(pos.size(), totalPurchaseValue, poByStatus,
            sos.size(), totalSalesValue, soByStatus);
    }
}
