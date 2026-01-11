package de.rollenspielwerkzeuge.dev.placemat.auth;

import de.rollenspielwerkzeuge.dev.placemat.auth.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @Value("${app.security.jwt.issuer:placemat-core}")
    private String issuer;

    @Value("${app.security.jwt.ttl-seconds:3600}")
    private long ttlSeconds;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(auth.getName())
                .claim("roles", auth.getAuthorities().stream().map(a -> a.getAuthority()).toList())
                .build();

        var headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse("Bearer", token, exp));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        return ResponseEntity.ok(new Object() {
            public final String username = authentication.getName();
        });
    }
}