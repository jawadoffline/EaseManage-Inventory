package com.easemanage.order.service;

import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.order.entity.PurchaseOrder;
import com.easemanage.order.entity.PurchaseOrderItem;
import com.easemanage.order.entity.SalesOrder;
import com.easemanage.order.entity.SalesOrderItem;
import com.easemanage.order.repository.PurchaseOrderRepository;
import com.easemanage.order.repository.SalesOrderRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPdfService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(22, 119, 255);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(240, 245, 255);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public byte[] generatePurchaseOrderPdf(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", id));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf);

        // Header
        doc.add(new Paragraph("PURCHASE ORDER")
            .setFontSize(24).setBold().setFontColor(PRIMARY_COLOR));
        doc.add(new Paragraph(po.getOrderNumber())
            .setFontSize(14).setFontColor(ColorConstants.GRAY));
        doc.add(new Paragraph("\n"));

        // Info table (2 columns)
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .useAllAvailableWidth();

        // Left column - Supplier info
        Cell leftCell = new Cell().setBorder(null);
        leftCell.add(new Paragraph("Supplier").setBold().setFontColor(PRIMARY_COLOR));
        leftCell.add(new Paragraph(po.getSupplier().getName()));
        if (po.getSupplier().getEmail() != null)
            leftCell.add(new Paragraph(po.getSupplier().getEmail()).setFontColor(ColorConstants.GRAY));
        infoTable.addCell(leftCell);

        // Right column - Order details
        Cell rightCell = new Cell().setBorder(null).setTextAlignment(TextAlignment.RIGHT);
        rightCell.add(new Paragraph("Status: " + po.getStatus().name()).setBold());
        rightCell.add(new Paragraph("Warehouse: " + po.getWarehouse().getName()));
        rightCell.add(new Paragraph("Date: " + po.getCreatedAt().format(DATETIME_FMT)));
        if (po.getExpectedDelivery() != null)
            rightCell.add(new Paragraph("Expected: " + po.getExpectedDelivery().format(DATE_FMT)));
        infoTable.addCell(rightCell);
        doc.add(infoTable);
        doc.add(new Paragraph("\n"));

        // Items table
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1}))
            .useAllAvailableWidth();

        String[] headers = {"Product", "SKU", "Qty", "Unit Cost", "Total"};
        for (String h : headers) {
            itemsTable.addHeaderCell(new Cell()
                .setBackgroundColor(HEADER_BG)
                .add(new Paragraph(h).setBold().setFontSize(10)));
        }

        for (PurchaseOrderItem item : po.getItems()) {
            itemsTable.addCell(new Cell().add(new Paragraph(item.getProduct().getName()).setFontSize(10)));
            itemsTable.addCell(new Cell().add(new Paragraph(item.getProduct().getSku()).setFontSize(9)));
            itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(10)));
            itemsTable.addCell(new Cell().add(new Paragraph("$" + item.getUnitCost()).setFontSize(10)));
            BigDecimal total = item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemsTable.addCell(new Cell().add(new Paragraph("$" + total).setFontSize(10)));
        }
        doc.add(itemsTable);

        // Total
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Total: $" + po.getTotalAmount())
            .setFontSize(16).setBold().setTextAlignment(TextAlignment.RIGHT));

        if (po.getNotes() != null) {
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Notes").setBold().setFontColor(PRIMARY_COLOR));
            doc.add(new Paragraph(po.getNotes()).setFontColor(ColorConstants.GRAY));
        }

        // Footer
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("Generated by EaseManage Inventory")
            .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    public byte[] generateSalesOrderPdf(Long id) {
        SalesOrder so = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order", id));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf);

        // Header
        doc.add(new Paragraph("SALES ORDER / INVOICE")
            .setFontSize(24).setBold().setFontColor(PRIMARY_COLOR));
        doc.add(new Paragraph(so.getOrderNumber())
            .setFontSize(14).setFontColor(ColorConstants.GRAY));
        doc.add(new Paragraph("\n"));

        // Info
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .useAllAvailableWidth();

        Cell leftCell = new Cell().setBorder(null);
        leftCell.add(new Paragraph("Bill To").setBold().setFontColor(PRIMARY_COLOR));
        leftCell.add(new Paragraph(so.getCustomerName()));
        if (so.getShippingAddress() != null)
            leftCell.add(new Paragraph(so.getShippingAddress()).setFontColor(ColorConstants.GRAY));
        infoTable.addCell(leftCell);

        Cell rightCell = new Cell().setBorder(null).setTextAlignment(TextAlignment.RIGHT);
        rightCell.add(new Paragraph("Status: " + so.getStatus().name()).setBold());
        rightCell.add(new Paragraph("Warehouse: " + so.getWarehouse().getName()));
        rightCell.add(new Paragraph("Date: " + so.getCreatedAt().format(DATETIME_FMT)));
        infoTable.addCell(rightCell);
        doc.add(infoTable);
        doc.add(new Paragraph("\n"));

        // Items
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1}))
            .useAllAvailableWidth();

        String[] headers = {"Product", "SKU", "Qty", "Unit Price", "Total"};
        for (String h : headers) {
            itemsTable.addHeaderCell(new Cell()
                .setBackgroundColor(HEADER_BG)
                .add(new Paragraph(h).setBold().setFontSize(10)));
        }

        for (SalesOrderItem item : so.getItems()) {
            itemsTable.addCell(new Cell().add(new Paragraph(item.getProduct().getName()).setFontSize(10)));
            itemsTable.addCell(new Cell().add(new Paragraph(item.getProduct().getSku()).setFontSize(9)));
            itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(10)));
            itemsTable.addCell(new Cell().add(new Paragraph("$" + item.getUnitPrice()).setFontSize(10)));
            BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemsTable.addCell(new Cell().add(new Paragraph("$" + total).setFontSize(10)));
        }
        doc.add(itemsTable);

        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Total: $" + so.getTotalAmount())
            .setFontSize(16).setBold().setTextAlignment(TextAlignment.RIGHT));

        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("Generated by EaseManage Inventory")
            .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }
}
