package com.tienda.universitaria.api.security.web;

import com.tienda.universitaria.api.security.domain.AppUser;
import com.tienda.universitaria.api.security.domain.Role;
import com.tienda.universitaria.api.security.dto.AuthDtos.*;
import com.tienda.universitaria.api.security.jwt.JwtService;
import com.tienda.universitaria.api.security.repo.AppUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository users;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerClient(@Valid @RequestBody RegisterClientRequest req) {
        return register(req.email(), req.password(), Set.of(Role.ROLE_CLIENT));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterAdminRequest req) {
        return register(req.email(), req.password(), Set.of(Role.ROLE_ADMIN));
    }

    @PostMapping("/register/coordinator")
    public ResponseEntity<AuthResponse> registerCoordinator(@Valid @RequestBody RegisterCoordinatorRequest req) {
        return register(req.email(), req.password(), Set.of(Role.ROLE_COORDINATOR));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        var user = users.findByEmailIgnoreCase(req.email()).orElseThrow();
        var principal = User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .build();
        var token = jwt.generateToken(principal, Map.of("roles", user.getRoles()));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }

    private ResponseEntity<AuthResponse> register(String email, String password, Set<Role> roles) {
        if (users.existsByEmailIgnoreCase(email))
            return ResponseEntity.badRequest().build();

        var user = AppUser.builder()
                .username(email)
                .password(encoder.encode(password))
                .roles(roles)
                .build();
        users.save(user);

        var principal = User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(roles.stream().map(Enum::name).toArray(String[]::new))
                .build();

        var token = jwt.generateToken(principal, Map.of("roles", roles));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }
}
