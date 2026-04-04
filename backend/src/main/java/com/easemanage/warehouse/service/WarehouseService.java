package com.easemanage.warehouse.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.warehouse.dto.WarehouseRequest;
import com.easemanage.warehouse.dto.WarehouseResponse;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.warehouse.repository.WarehouseRepository;
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
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<WarehouseResponse> getWarehouses(int page, int size, String search) {
        Page<Warehouse> warehouses = warehouseRepository.search(search,
            PageRequest.of(page, size, Sort.by("name")));
        return new PagedResponse<>(
            warehouses.getContent().stream().map(this::toResponse).toList(),
            warehouses.getNumber(), warehouses.getSize(),
            warehouses.getTotalElements(), warehouses.getTotalPages(), warehouses.isLast()
        );
    }

    @Cacheable("warehouses")
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllActive() {
        return warehouseRepository.findAll(Sort.by("name")).stream()
            .filter(w -> Boolean.TRUE.equals(w.getIsActive()))
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        return toResponse(findById(id));
    }

    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Warehouse with code '" + request.code() + "' already exists");
        }
        Warehouse warehouse = Warehouse.builder()
            .name(request.name())
            .code(request.code().toUpperCase())
            .address(request.address())
            .city(request.city())
            .state(request.state())
            .country(request.country())
            .capacity(request.capacity())
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();
        Warehouse saved = warehouseRepository.save(warehouse);
        auditService.log("Warehouse", saved.getId(), "CREATE", null, "name=" + saved.getName());
        return toResponse(saved);
    }

    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = findById(id);
        if (!warehouse.getCode().equals(request.code().toUpperCase()) &&
            warehouseRepository.existsByCode(request.code().toUpperCase())) {
            throw new DuplicateResourceException("Warehouse with code '" + request.code() + "' already exists");
        }
        warehouse.setName(request.name());
        warehouse.setCode(request.code().toUpperCase());
        warehouse.setAddress(request.address());
        warehouse.setCity(request.city());
        warehouse.setState(request.state());
        warehouse.setCountry(request.country());
        warehouse.setCapacity(request.capacity());
        if (request.isActive() != null) warehouse.setIsActive(request.isActive());
        Warehouse updated = warehouseRepository.save(warehouse);
        auditService.log("Warehouse", id, "UPDATE", null, "name=" + updated.getName());
        return toResponse(updated);
    }

    @CacheEvict(value = "warehouses", allEntries = true)
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = findById(id);
        auditService.log("Warehouse", id, "DELETE", "name=" + warehouse.getName(), null);
        warehouseRepository.delete(warehouse);
    }

    private Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return new WarehouseResponse(w.getId(), w.getName(), w.getCode(),
            w.getAddress(), w.getCity(), w.getState(), w.getCountry(),
            w.getCapacity(), w.getIsActive(), w.getCreatedAt());
    }
}
