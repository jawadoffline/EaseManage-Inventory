package com.easemanage.customer.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.customer.dto.CustomerRequest;
import com.easemanage.customer.dto.CustomerResponse;
import com.easemanage.customer.entity.Customer;
import com.easemanage.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getCustomers(int page, int size, String search) {
        Page<Customer> customers = customerRepository.search(search,
            PageRequest.of(page, size, Sort.by("name")));
        return new PagedResponse<>(
            customers.getContent().stream().map(this::toResponse).toList(),
            customers.getNumber(), customers.getSize(),
            customers.getTotalElements(), customers.getTotalPages(), customers.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllActive() {
        return customerRepository.findByIsActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return toResponse(findById(id));
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = Customer.builder()
            .name(request.name()).email(request.email()).phone(request.phone())
            .address(request.address()).city(request.city()).country(request.country())
            .contactPerson(request.contactPerson()).notes(request.notes())
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();
        Customer saved = customerRepository.save(customer);
        auditService.log("Customer", saved.getId(), "CREATE", null, "name=" + saved.getName());
        return toResponse(saved);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer c = findById(id);
        c.setName(request.name()); c.setEmail(request.email()); c.setPhone(request.phone());
        c.setAddress(request.address()); c.setCity(request.city()); c.setCountry(request.country());
        c.setContactPerson(request.contactPerson()); c.setNotes(request.notes());
        if (request.isActive() != null) c.setIsActive(request.isActive());
        Customer updated = customerRepository.save(c);
        auditService.log("Customer", id, "UPDATE", null, "name=" + updated.getName());
        return toResponse(updated);
    }

    public void deleteCustomer(Long id) {
        Customer customer = findById(id);
        auditService.log("Customer", id, "DELETE", "name=" + customer.getName(), null);
        customerRepository.delete(customer);
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(),
            c.getAddress(), c.getCity(), c.getCountry(), c.getContactPerson(),
            c.getNotes(), c.getIsActive(), c.getCreatedAt());
    }
}
