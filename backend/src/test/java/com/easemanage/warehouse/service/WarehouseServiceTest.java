package com.easemanage.warehouse.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.warehouse.dto.WarehouseRequest;
import com.easemanage.warehouse.dto.WarehouseResponse;
import com.easemanage.warehouse.entity.Warehouse;
import com.easemanage.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void createWarehouse_success() {
        WarehouseRequest request = new WarehouseRequest("Main Warehouse", "WH001",
                "123 Street", "City", "State", "Country", 1000, true);
        Warehouse saved = buildWarehouse(1L, "Main Warehouse", "WH001");

        when(warehouseRepository.existsByCode("WH001")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertEquals("Main Warehouse", response.name());
        assertEquals("WH001", response.code());
        verify(warehouseRepository).save(any(Warehouse.class));
        verify(auditService).log(eq("Warehouse"), eq(1L), eq("CREATE"), isNull(), eq("name=Main Warehouse"));
    }

    @Test
    void createWarehouse_duplicateCode_throwsDuplicateResourceException() {
        WarehouseRequest request = new WarehouseRequest("Warehouse", "WH001",
                null, null, null, null, null, null);

        when(warehouseRepository.existsByCode("WH001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> warehouseService.createWarehouse(request));
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void getWarehouseById_notFound_throwsResourceNotFoundException() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warehouseService.getWarehouseById(99L));
    }

    @Test
    void updateWarehouse_success() {
        Warehouse existing = buildWarehouse(1L, "Old Name", "WH001");
        WarehouseRequest request = new WarehouseRequest("New Name", "WH001",
                "New Addr", "New City", "New State", "New Country", 2000, true);
        Warehouse updated = buildWarehouse(1L, "New Name", "WH001");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updated);

        WarehouseResponse response = warehouseService.updateWarehouse(1L, request);

        assertEquals("New Name", response.name());
        verify(auditService).log(eq("Warehouse"), eq(1L), eq("UPDATE"), isNull(), eq("name=New Name"));
    }

    @Test
    void deleteWarehouse_success() {
        Warehouse warehouse = buildWarehouse(1L, "Warehouse", "WH001");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        warehouseService.deleteWarehouse(1L);

        verify(warehouseRepository).delete(warehouse);
        verify(auditService).log(eq("Warehouse"), eq(1L), eq("DELETE"), eq("name=Warehouse"), isNull());
    }

    private Warehouse buildWarehouse(Long id, String name, String code) {
        Warehouse w = Warehouse.builder()
                .id(id)
                .name(name)
                .code(code)
                .address("addr")
                .city("city")
                .state("state")
                .country("country")
                .capacity(1000)
                .isActive(true)
                .build();
        w.setCreatedAt(LocalDateTime.now());
        return w;
    }
}
