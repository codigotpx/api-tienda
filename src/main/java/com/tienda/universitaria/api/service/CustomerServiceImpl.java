package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.CustomerDtos;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerDtos.CustomerResponse create(CustomerDtos.CustomerCreateRequest req) {
        if (req == null) {
            throw new ValidationException("CustomerCreateRequest must not be null");
        }
        if (req.email() == null || req.email().isBlank()) {
            throw new ValidationException("email must not be blank");
        }
        if (customerRepository.existsByEmail(req.email())) {
            throw new ConflictException("Customer email already exists: " + req.email());
        }

        Customer customer = customerMapper.toEntity(req);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    public CustomerDtos.CustomerResponse update(UUID id, CustomerDtos.CustomerUpdateRequest req) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }
        if (req == null) {
            throw new ValidationException("CustomerUpdateRequest must not be null");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        if (req.email() != null && !req.email().isBlank()) {
            String newEmail = req.email();
            String currentEmail = customer.getEmail();
            if (!newEmail.equalsIgnoreCase(currentEmail) && customerRepository.existsByEmail(newEmail)) {
                throw new ConflictException("Customer email already exists: " + newEmail);
            }
        }

        customerMapper.patch(customer, req);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDtos.CustomerResponse get(UUID id) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDtos.CustomerResponse getByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("email must not be blank");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + email));
        return customerMapper.toResponse(customer);
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
        if (status == null) {
            throw new ValidationException("status must not be null");
        }

        return customerRepository.findByStatus(status).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerDtos.CustomerResponse setStatus(UUID id, CustomerStatus status) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }
        if (status == null) {
            throw new ValidationException("status must not be null");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        customer.setStatus(status);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        customerRepository.delete(customer);
    }
}
