package com.easemanage.supplier.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import com.easemanage.supplier.dto.SupplierProductRequest;
import com.easemanage.supplier.dto.SupplierProductResponse;
import com.easemanage.supplier.dto.SupplierRequest;
import com.easemanage.supplier.dto.SupplierResponse;
import com.easemanage.supplier.entity.Supplier;
import com.easemanage.supplier.entity.SupplierProduct;
import com.easemanage.supplier.repository.SupplierProductRepository;
import com.easemanage.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<SupplierResponse> getSuppliers(int page, int size, String search) {
        Page<Supplier> suppliers = supplierRepository.search(search,
            PageRequest.of(page, size, Sort.by("name")));
        return new PagedResponse<>(
            suppliers.getContent().stream().map(this::toResponse).toList(),
            suppliers.getNumber(), suppliers.getSize(),
            suppliers.getTotalElements(), suppliers.getTotalPages(), suppliers.isLast()
        );
    }

    @Cacheable("suppliers")
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllActive() {
        return supplierRepository.findByIsActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        return toResponse(findById(id));
    }

    @CacheEvict(value = "suppliers", allEntries = true)
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
            .name(request.name()).email(request.email()).phone(request.phone())
            .address(request.address()).city(request.city()).country(request.country())
            .contactPerson(request.contactPerson()).paymentTerms(request.paymentTerms())
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();
        Supplier saved = supplierRepository.save(supplier);
        auditService.log("Supplier", saved.getId(), "CREATE", null, "name=" + saved.getName());
        return toResponse(saved);
    }

    @CacheEvict(value = "suppliers", allEntries = true)
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier s = findById(id);
        s.setName(request.name()); s.setEmail(request.email()); s.setPhone(request.phone());
        s.setAddress(request.address()); s.setCity(request.city()); s.setCountry(request.country());
        s.setContactPerson(request.contactPerson()); s.setPaymentTerms(request.paymentTerms());
        if (request.isActive() != null) s.setIsActive(request.isActive());
        Supplier updated = supplierRepository.save(s);
        auditService.log("Supplier", id, "UPDATE", null, "name=" + updated.getName());
        return toResponse(updated);
    }

    @CacheEvict(value = "suppliers", allEntries = true)
    public void deleteSupplier(Long id) {
        Supplier supplier = findById(id);
        auditService.log("Supplier", id, "DELETE", "name=" + supplier.getName(), null);
        supplierRepository.delete(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierProductResponse> getSupplierProducts(Long supplierId) {
        findById(supplierId); // validate supplier exists
        return supplierProductRepository.findBySupplierId(supplierId).stream()
            .map(this::toSupplierProductResponse).toList();
    }

    public SupplierProductResponse addProductToSupplier(Long supplierId, SupplierProductRequest request) {
        Supplier supplier = findById(supplierId);
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        if (supplierProductRepository.existsBySupplierIdAndProductId(supplierId, request.productId())) {
            throw new DuplicateResourceException("Product already linked to this supplier");
        }
        SupplierProduct sp = SupplierProduct.builder()
            .supplier(supplier).product(product)
            .supplierSku(request.supplierSku())
            .leadTimeDays(request.leadTimeDays())
            .unitCost(request.unitCost())
            .build();
        return toSupplierProductResponse(supplierProductRepository.save(sp));
    }

    public void removeProductFromSupplier(Long supplierId, Long supplierProductId) {
        supplierProductRepository.deleteById(supplierProductId);
    }

    private Supplier findById(Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(s.getId(), s.getName(), s.getEmail(), s.getPhone(),
            s.getAddress(), s.getCity(), s.getCountry(), s.getContactPerson(),
            s.getPaymentTerms(), s.getIsActive(), s.getCreatedAt());
    }

    private SupplierProductResponse toSupplierProductResponse(SupplierProduct sp) {
        return new SupplierProductResponse(sp.getId(),
            sp.getSupplier().getId(), sp.getSupplier().getName(),
            sp.getProduct().getId(), sp.getProduct().getName(), sp.getProduct().getSku(),
            sp.getSupplierSku(), sp.getLeadTimeDays(), sp.getUnitCost());
    }
}
