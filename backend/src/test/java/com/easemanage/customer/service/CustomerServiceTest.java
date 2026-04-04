package com.easemanage.customer.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.customer.dto.CustomerRequest;
import com.easemanage.customer.dto.CustomerResponse;
import com.easemanage.customer.entity.Customer;
import com.easemanage.customer.repository.CustomerRepository;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_success() {
        CustomerRequest request = new CustomerRequest("John Doe", "john@test.com", "555-1234",
                "123 St", "City", "Country", "Jane", "VIP customer", true);
        Customer saved = buildCustomer(1L, "John Doe");

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse response = customerService.createCustomer(request);

        assertEquals("John Doe", response.name());
        assertEquals(1L, response.id());
        verify(customerRepository).save(any(Customer.class));
        verify(auditService).log(eq("Customer"), eq(1L), eq("CREATE"), isNull(), eq("name=John Doe"));
    }

    @Test
    void getCustomerById_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
    }

    @Test
    void deleteCustomer_success() {
        Customer customer = buildCustomer(1L, "John Doe");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
        verify(auditService).log(eq("Customer"), eq(1L), eq("DELETE"), eq("name=John Doe"), isNull());
    }

    @Test
    void updateCustomer_success() {
        Customer existing = buildCustomer(1L, "Old Name");
        CustomerRequest request = new CustomerRequest("New Name", "new@test.com", "555-9999",
                "New St", "New City", "New Country", "New Contact", "Updated notes", true);
        Customer updated = buildCustomer(1L, "New Name");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenReturn(updated);

        CustomerResponse response = customerService.updateCustomer(1L, request);

        assertEquals("New Name", response.name());
        verify(auditService).log(eq("Customer"), eq(1L), eq("UPDATE"), isNull(), eq("name=New Name"));
    }

    @Test
    void getCustomerById_found_returnsResponse() {
        Customer customer = buildCustomer(1L, "John Doe");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertEquals("John Doe", response.name());
        assertEquals(1L, response.id());
    }

    private Customer buildCustomer(Long id, String name) {
        Customer c = Customer.builder()
                .id(id)
                .name(name)
                .email("test@test.com")
                .phone("555-0000")
                .address("addr")
                .city("city")
                .country("country")
                .contactPerson("contact")
                .notes("notes")
                .isActive(true)
                .build();
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
