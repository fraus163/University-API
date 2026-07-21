package com.fraus.spring.universityapi.auth.web.dto;

public record JwtResponse(
        String jwtToken,
        String role
) {
}
