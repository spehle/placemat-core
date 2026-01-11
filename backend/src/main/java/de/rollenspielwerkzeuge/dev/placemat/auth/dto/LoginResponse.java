package de.rollenspielwerkzeuge.dev.placemat.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String tokenType,
        String token,
        Instant expiresAt
) {}
