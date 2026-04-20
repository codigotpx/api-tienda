package com.tienda.universitaria.api.security.config;

import com.tienda.universitaria.api.security.error.Http401EntryPoint;
import com.tienda.universitaria.api.security.error.Http403AccessDenied;
import com.tienda.universitaria.api.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final Http401EntryPoint authEntryPoint;
    private final Http403AccessDenied accessDenied;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDenied))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()

                        .requestMatchers("/api/auth/register/admin").hasRole("ADMIN")
                        .requestMatchers("/api/auth/register/coordinator").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()

                        .requestMatchers(HttpMethod.POST,   "/api/controllers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/controllers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/controllers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/controllers/**").authenticated()

                        .requestMatchers(HttpMethod.POST,   "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/products/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/inventories/**").hasAnyRole("ADMIN", "COORDINATOR")
                        .requestMatchers("/api/inventories/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,    "/api/customers/**").hasAnyRole("ADMIN", "CLIENT")
                        .requestMatchers(HttpMethod.PUT,    "/api/customers/**").hasAnyRole("ADMIN", "CLIENT")
                        .requestMatchers(HttpMethod.PATCH,  "/api/customers/**").hasAnyRole("ADMIN", "CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers("/api/addresses/**").hasAnyRole("ADMIN", "CLIENT")

                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("ADMIN", "CLIENT", "USER")

                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "CLIENT")

                        .requestMatchers("/api/orders/*/items/**").hasAnyRole("ADMIN", "CLIENT", "USER")
                        .requestMatchers("/api/orderItems/**").hasAnyRole("ADMIN", "CLIENT", "USER")

                        .requestMatchers(HttpMethod.GET, "/api/orders/*/history").hasAnyRole("ADMIN", "CLIENT")

                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "COORDINATOR")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
