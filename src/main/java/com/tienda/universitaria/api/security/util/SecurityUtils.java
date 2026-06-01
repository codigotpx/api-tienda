package com.tienda.universitaria.api.security.util;

import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final CustomerRepository customerRepository;

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ValidationException("Not authenticated");
        }
        return auth.getName();
    }

    public Customer getCurrentCustomer() {
        String email = getCurrentUsername();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + email));
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(role));
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isClient() {
        return hasRole("ROLE_CLIENT");
    }
}
