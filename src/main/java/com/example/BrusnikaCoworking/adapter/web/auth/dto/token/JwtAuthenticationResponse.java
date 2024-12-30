package com.example.BrusnikaCoworking.adapter.web.auth.dto.token;

import com.example.BrusnikaCoworking.domain.user.Role;

public record JwtAuthenticationResponse(String tokenAccess, String tokenRefresh, Role role) {
}