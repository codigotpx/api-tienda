package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByCustomerId(UUID customerId);

    boolean existsByIdAndCustomerId(UUID addressId, UUID customerId);
}
