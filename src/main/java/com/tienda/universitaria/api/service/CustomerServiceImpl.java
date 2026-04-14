package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.CustomerDtos;
import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerDtos.CustomerResponse create(CustomerDtos.CustomerCreateRequest req) {
        if (customerRepository.existsByEmail(req.email()))
            throw new ConflictException("Customer email already exists: " + req.email());

        Customer customer = customerMapper.toEntity(req);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerDtos.CustomerResponse update(UUID id, CustomerDtos.CustomerUpdateRequest req) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        if (req.email() != null && !req.email().isBlank()) {
            String newEmail = req.email();
            if (!newEmail.equalsIgnoreCase(customer.getEmail()) && customerRepository.existsByEmail(newEmail))
                throw new ConflictException("Customer email already exists: " + newEmail);
        }

        customerMapper.patch(customer, req);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDtos.CustomerResponse get(UUID id) {
        return customerRepository.findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDtos.CustomerResponse getByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDtos.CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDtos.CustomerResponse> getByStatus(CustomerStatus status) {
        return customerRepository.findByStatus(status).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerDtos.CustomerResponse setStatus(UUID id, CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        customer.setStatus(status);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        customerRepository.delete(customer);
    }
}