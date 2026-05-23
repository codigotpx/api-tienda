package com.tienda.universitaria.api.security.service;

import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.security.domain.AppUser;
import com.tienda.universitaria.api.security.domain.Role;
import com.tienda.universitaria.api.security.dto.AuthDtos;
import com.tienda.universitaria.api.security.jwt.JwtService;
import com.tienda.universitaria.api.security.repo.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository users;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final CustomerRepository customers;

    @Override
    public AuthDtos.AuthResponse registerClientWithProfile(AuthDtos.RegisterClientWithProfileRequest req) {
        if (req == null) throw new ValidationException("RegisterClientWithProfileRequest must not be null");

        String email = req.email();
        if (email == null || email.isBlank()) throw new ValidationException("email must not be blank");

        if (users.existsByUsernameIgnoreCase(email)) {
            throw new ConflictException("User email already exists: " + email);
        }
        if (customers.existsByEmail(email)) {
            throw new ConflictException("Customer email already exists: " + email);
        }

        var roles = Set.of(Role.ROLE_CLIENT);

        AppUser user = AppUser.builder()
                .username(email)
                .password(encoder.encode(req.password()))
                .roles(roles)
                .build();
        AppUser savedUser = users.save(user);

        Customer customer = Customer.builder()
                .user(savedUser)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .email(email)
                .build();
        customers.save(customer);

        var principal = User.withUsername(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities(roles.stream().map(Enum::name).toArray(String[]::new))
                .build();

        var token = jwt.generateToken(principal, Map.of(
                "roles", roles,
                "userId", savedUser.getId()
        ));
        return new AuthDtos.AuthResponse(token, "Bearer", jwt.getExpirationSeconds());
    }
}

