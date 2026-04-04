package com.easemanage.supplier.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.supplier.dto.SupplierRequest;
import com.easemanage.supplier.dto.SupplierResponse;
import com.easemanage.supplier.entity.Supplier;
import com.easemanage.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void createSupplier_success() {
        SupplierRequest request = new SupplierRequest("Acme Corp", "acme@test.com", "123456",
                "123 St", "City", "Country", "John", "Net 30", true);
        Supplier saved = buildSupplier(1L, "Acme Corp");

        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        SupplierResponse response = supplierService.createSupplier(request);

        assertEquals("Acme Corp", response.name());
        assertEquals(1L, response.id());
        verify(supplierRepository).save(any(Supplier.class));
        verify(auditService).log(eq("Supplier"), eq(1L), eq("CREATE"), isNull(), eq("name=Acme Corp"));
    }

    @Test
    void getSupplierById_notFound_throwsResourceNotFoundException() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.getSupplierById(99L));
    }

    @Test
    void deleteSupplier_success() {
        Supplier supplier = buildSupplier(1L, "Acme Corp");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        supplierService.deleteSupplier(1L);

        verify(supplierRepository).delete(supplier);
        verify(auditService).log(eq("Supplier"), eq(1L), eq("DELETE"), eq("name=Acme Corp"), isNull());
    }

    @Test
    void getSuppliers_returnsPagedResponse() {
        Supplier supplier = buildSupplier(1L, "Supplier1");
        Page<Supplier> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 10, Sort.by("name")), 1);

        when(supplierRepository.search(eq("test"), any(Pageable.class))).thenReturn(page);

        PagedResponse<SupplierResponse> result = supplierService.getSuppliers(0, 10, "test");

        assertEquals(1, result.content().size());
        assertEquals("Supplier1", result.content().get(0).name());
        assertEquals(1, result.totalElements());
        assertTrue(result.last());
    }

    @Test
    void updateSupplier_success() {
        Supplier existing = buildSupplier(1L, "Old Name");
        SupplierRequest request = new SupplierRequest("New Name", "new@test.com", "999",
                "New St", "New City", "New Country", "Jane", "Net 60", true);
        Supplier updated = buildSupplier(1L, "New Name");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updated);

        SupplierResponse response = supplierService.updateSupplier(1L, request);

        assertEquals("New Name", response.name());
        verify(auditService).log(eq("Supplier"), eq(1L), eq("UPDATE"), isNull(), eq("name=New Name"));
    }

    private Supplier buildSupplier(Long id, String name) {
        Supplier s = Supplier.builder()
                .id(id)
                .name(name)
                .email("test@test.com")
                .phone("123")
                .address("addr")
                .city("city")
                .country("country")
                .contactPerson("contact")
                .paymentTerms("Net 30")
                .isActive(true)
                .build();
        s.setCreatedAt(LocalDateTime.now());
        return s;
    }
}
