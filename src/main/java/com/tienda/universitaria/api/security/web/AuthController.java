package com.tienda.universitaria.api.security.web;

import com.tienda.universitaria.api.security.domain.AppUser;
import com.tienda.universitaria.api.security.domain.Role;
import com.tienda.universitaria.api.security.dto.AuthDtos.*;
import com.tienda.universitaria.api.security.jwt.JwtService;
import com.tienda.universitaria.api.security.repo.AppUserRepository;
import com.tienda.universitaria.api.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;

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
    private final AuthService authService;

    @Value("${app.auth.cookie-name:ACCESS_TOKEN}")
    private String authCookieName;

    @Value("${app.auth.cookie-secure:false}")
    private boolean authCookieSecure;

    @Value("${app.auth.cookie-samesite:Lax}")
    private String authCookieSameSite;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerClient(@Valid @RequestBody RegisterClientRequest req) {
        return register(req.email(), req.password(), Set.of(Role.ROLE_CLIENT));
    }

    // Professional client signup: creates AppUser + Customer profile in one transaction.
    @PostMapping("/register/client")
    public ResponseEntity<AuthResponse> registerClientWithProfile(@Valid @RequestBody RegisterClientWithProfileRequest req) {
        return ResponseEntity.ok(authService.registerClientWithProfile(req));
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
        var user = users.findByUsernameIgnoreCase(req.email()).orElseThrow();
        var principal = User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .build();
        var token = jwt.generateToken(principal, Map.of("roles", user.getRoles()));

        var cookie = buildAuthCookie(token, jwt.getExpirationSeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        var cookie = clearAuthCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        var username = authentication.getName();
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(new MeResponse(username, roles));
    }

    private ResponseEntity<AuthResponse> register(String email, String password, Set<Role> roles) {
        if (users.existsByUsernameIgnoreCase(email))
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
        var cookie = buildAuthCookie(token, jwt.getExpirationSeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }

    private ResponseCookie buildAuthCookie(String token, long expiresInSeconds) {
        // Cookie is HttpOnly so the frontend can't read it; browser sends it automatically.
        return ResponseCookie.from(authCookieName, token)
                .httpOnly(true)
                .secure(authCookieSecure)
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(expiresInSeconds)
                .build();
    }

    private ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(authCookieName, "")
                .httpOnly(true)
                .secure(authCookieSecure)
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(0)
                .build();
    }
}
