package com.easemanage.product.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.category.entity.Category;
import com.easemanage.category.repository.CategoryRepository;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.product.dto.ProductRequest;
import com.easemanage.product.dto.ProductResponse;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    private static final AtomicLong skuCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(int page, int size, String search, Long categoryId, Boolean isActive) {
        Page<Product> products = productRepository.search(search, categoryId, isActive,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
            products.getContent().stream().map(this::toResponse).toList(),
            products.getNumber(), products.getSize(),
            products.getTotalElements(), products.getTotalPages(), products.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(findById(id));
    }

    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Product with name '" + request.name() + "' already exists");
        }
        Product product = Product.builder()
            .sku(generateSku())
            .name(request.name())
            .description(request.description())
            .unitOfMeasure(request.unitOfMeasure() != null ? request.unitOfMeasure() : "PCS")
            .minStockLevel(request.minStockLevel() != null ? request.minStockLevel() : 0)
            .maxStockLevel(request.maxStockLevel())
            .reorderPoint(request.reorderPoint() != null ? request.reorderPoint() : 0)
            .costPrice(request.costPrice() != null ? request.costPrice() : java.math.BigDecimal.ZERO)
            .sellingPrice(request.sellingPrice() != null ? request.sellingPrice() : java.math.BigDecimal.ZERO)
            .barcode(request.barcode())
            .imageUrl(request.imageUrl())
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();
        if (request.categoryId() != null) {
            product.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId())));
        }
        Product saved = productRepository.save(product);
        auditService.log("Product", saved.getId(), "CREATE", null, "name=" + saved.getName());
        return toResponse(saved);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findById(id);
        product.setName(request.name());
        product.setDescription(request.description());
        if (request.unitOfMeasure() != null) product.setUnitOfMeasure(request.unitOfMeasure());
        if (request.minStockLevel() != null) product.setMinStockLevel(request.minStockLevel());
        product.setMaxStockLevel(request.maxStockLevel());
        if (request.reorderPoint() != null) product.setReorderPoint(request.reorderPoint());
        if (request.costPrice() != null) product.setCostPrice(request.costPrice());
        if (request.sellingPrice() != null) product.setSellingPrice(request.sellingPrice());
        product.setBarcode(request.barcode());
        product.setImageUrl(request.imageUrl());
        if (request.isActive() != null) product.setIsActive(request.isActive());
        if (request.categoryId() != null) {
            product.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId())));
        } else {
            product.setCategory(null);
        }
        Product updated = productRepository.save(product);
        auditService.log("Product", id, "UPDATE", null, "name=" + updated.getName());
        return toResponse(updated);
    }

    public void deleteProduct(Long id) {
        Product product = findById(id);
        auditService.log("Product", id, "DELETE", "name=" + product.getName(), null);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return productRepository.countByIsActiveTrue();
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private String generateSku() {
        return "PRD-" + String.format("%06d", skuCounter.incrementAndGet());
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
            p.getId(), p.getSku(), p.getName(), p.getDescription(),
            p.getCategory() != null ? p.getCategory().getId() : null,
            p.getCategory() != null ? p.getCategory().getName() : null,
            p.getUnitOfMeasure(), p.getMinStockLevel(), p.getMaxStockLevel(),
            p.getReorderPoint(), p.getCostPrice(), p.getSellingPrice(),
            p.getBarcode(), p.getImageUrl(), p.getIsActive(),
            null, p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
