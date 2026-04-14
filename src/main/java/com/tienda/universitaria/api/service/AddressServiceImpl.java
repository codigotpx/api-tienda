package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.AddressDtos;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.repositories.AddressRepository;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.service.mapper.AddressMapper;
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
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        Address address = addressMapper.toEntity(req);
        address.setCustomer(customer);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public AddressDtos.AddressResponse update(UUID customerId, UUID addressId, AddressDtos.AddressUpdateRequest req) {
        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId))
            throw new ResourceNotFoundException("Address not found for customer. customerId=%s addressId=%s"
                    .formatted(customerId, addressId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        addressMapper.patch(address, req);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDtos.AddressResponse get(UUID addressId) {
        return addressRepository.findById(addressId)
                .map(addressMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDtos.AddressResponse> getByCustomer(UUID customerId) {
        if (!customerRepository.existsById(customerId))
            throw new ResourceNotFoundException("Customer not found: " + customerId);

        return addressRepository.findByCustomerId(customerId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID customerId, UUID addressId) {
        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId))
            throw new ResourceNotFoundException("Address not found for customer. customerId=%s addressId=%s"
                    .formatted(customerId, addressId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
        addressRepository.delete(address);
    }
}