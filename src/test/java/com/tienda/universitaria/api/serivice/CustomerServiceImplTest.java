package com.tienda.universitaria.api.serivice;

import com.tienda.universitaria.api.api.dto.CustomerDtos;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.service.CustomerServiceImpl;
import com.tienda.universitaria.api.service.mapper.CustomerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    CustomerServiceImpl customerServiceImpl;

    @Test
    void shouldCreateAndReturnResponseDto() {
        var req = new CustomerDtos.CustomerCreateRequest("Juan", "Perez", "123", "juan@test.com");

        var entity = Customer.builder()
                .firstName("Juan")
                .lastName("Perez")
                .phone("123")
                .email("juan@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        var saved = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Perez")
                .phone("123")
                .email("juan@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        var expected = new CustomerDtos.CustomerResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getStatus()
        );

        when(customerRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(customerMapper.toEntity(req)).thenReturn(entity);
        when(customerRepository.save(entity)).thenReturn(saved);
        when(customerMapper.toResponse(saved)).thenReturn(expected);

        var actual = customerServiceImpl.create(req);

        assertEquals(expected, actual);
        verify(customerRepository).existsByEmail("juan@test.com");
        verify(customerMapper).toEntity(req);
        verify(customerRepository).save(entity);
        verify(customerMapper).toResponse(saved);
    }

    @Test
    void shouldRejectCreateWhenEmailAlreadyExists() {
        var req = new CustomerDtos.CustomerCreateRequest("Juan", "Perez", "123", "juan@test.com");

        when(customerRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> customerServiceImpl.create(req));

        verify(customerRepository).existsByEmail("juan@test.com");
        verify(customerRepository, never()).save(any());
        verify(customerMapper, never()).toEntity(any());
    }

    @Test
    void shouldGetCustomerById() {
        UUID id = UUID.randomUUID();
        var customer = Customer.builder().id(id).email("a@b.com").firstName("A").lastName("B").phone("1").status(CustomerStatus.ACTIVE).build();
        var expected = new CustomerDtos.CustomerResponse(id, "A", "B", "a@b.com", "1", CustomerStatus.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(expected);

        var actual = customerServiceImpl.get(id);

        assertEquals(expected, actual);
        verify(customerRepository).findById(id);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldUpdateCustomerAndCallPatch() {
        UUID id = UUID.randomUUID();
        var existing = Customer.builder().id(id).email("old@test.com").firstName("Old").lastName("Name").phone("1").status(CustomerStatus.ACTIVE).build();
        var req = new CustomerDtos.CustomerUpdateRequest("New", null, null, "new@test.com");
        var expected = new CustomerDtos.CustomerResponse(id, "New", "Name", "new@test.com", "1", CustomerStatus.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerMapper.toResponse(existing)).thenReturn(expected);

        var actual = customerServiceImpl.update(id, req);

        assertEquals(expected, actual);
        verify(customerRepository).findById(id);
        verify(customerRepository).existsByEmail("new@test.com");
        verify(customerMapper).patch(eq(existing), eq(req));
        verify(customerRepository).save(existing);
    }

    @Test
    void shouldRejectUpdateWhenNewEmailAlreadyExists() {
        UUID id = UUID.randomUUID();
        var existing = Customer.builder().id(id).email("old@test.com").firstName("Old").lastName("Name").phone("1").status(CustomerStatus.ACTIVE).build();
        var req = new CustomerDtos.CustomerUpdateRequest(null, null, null, "new@test.com");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmail("new@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> customerServiceImpl.update(id, req));

        verify(customerRepository).findById(id);
        verify(customerRepository).existsByEmail("new@test.com");
        verify(customerMapper, never()).patch(any(), any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCustomer() {
        UUID id = UUID.randomUUID();
        var existing = Customer.builder().id(id).email("old@test.com").firstName("Old").lastName("Name").phone("1").status(CustomerStatus.ACTIVE).build();

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        customerServiceImpl.delete(id);

        verify(customerRepository).findById(id);
        verify(customerRepository).delete(existing);
    }
}
