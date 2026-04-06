package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.AddressDtos;
import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.repositories.AddressRepository;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.service.mapper.AddressMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressDtos.AddressResponse create(UUID customerId, AddressDtos.AddressCreateRequest req) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("AddressCreateRequest must not be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));

        Address address = addressMapper.toEntity(req);
        address.setCustomer(customer);

        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    public AddressDtos.AddressResponse update(UUID customerId, UUID addressId, AddressDtos.AddressUpdateRequest req) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (addressId == null) {
            throw new IllegalArgumentException("addressId must not be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("AddressUpdateRequest must not be null");
        }

        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId)) {
            throw new EntityNotFoundException("Address not found for customer. customerId=%s addressId=%s"
                    .formatted(customerId, addressId));
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + addressId));

        addressMapper.patch(address, req);
        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDtos.AddressResponse get(UUID addressId) {
        if (addressId == null) {
            throw new IllegalArgumentException("addressId must not be null");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + addressId));
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDtos.AddressResponse> getByCustomer(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }

        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found: " + customerId);
        }

        return addressRepository.findByCustomerId(customerId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID customerId, UUID addressId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (addressId == null) {
            throw new IllegalArgumentException("addressId must not be null");
        }

        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId)) {
            throw new EntityNotFoundException("Address not found for customer. customerId=%s addressId=%s"
                    .formatted(customerId, addressId));
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + addressId));
        addressRepository.delete(address);
    }
}

