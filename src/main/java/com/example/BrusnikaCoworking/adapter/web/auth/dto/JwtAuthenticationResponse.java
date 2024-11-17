package com.example.BrusnikaCoworking.adapter.web.auth.dto;

import com.example.BrusnikaCoworking.domain.user.Role;

public record JwtAuthenticationResponse(String tokenAccess, String tokeRefresh, Role role) {
}