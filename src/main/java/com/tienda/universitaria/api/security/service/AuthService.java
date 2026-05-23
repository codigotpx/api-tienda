package com.tienda.universitaria.api.security.service;

import com.tienda.universitaria.api.security.dto.AuthDtos;

public interface AuthService {
    AuthDtos.AuthResponse registerClientWithProfile(AuthDtos.RegisterClientWithProfileRequest req);
}

