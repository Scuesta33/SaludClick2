package com.example.SaludClick.securityConfig;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtils {

    @Value("${security.jwt.private.key}")
    private String secretKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator; // Generador del token (issuer)

    private static final long EXPIRATION_TIME_MS = 30 * 60 * 1000; // 30 minutos

    public String createToken(Authentication authentication, Long userId) {
        System.out.println("Generando token JWT...");

        if (authentication == null || userId == null) {
            System.out.println("Error: Autenticación o UserID es nulo.");
            throw new IllegalArgumentException("Autenticación y UserID no pueden ser nulos");
        }

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        try {
            String token = JWT.create()
                    .withIssuer(userGenerator)
                    .withSubject(email)
                    .withClaim("roles", roles)
                    .withClaim("userId", userId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                    .withJWTId(UUID.randomUUID().toString())
                    .withNotBefore(new Date())
                    .sign(algorithm);

            System.out.println("Token generado con éxito.");
            return token;

        } catch (Exception e) {
            System.out.println("Error al generar el token: " + e.getMessage());
            throw new RuntimeException("No se pudo generar el token");
        }
    }

    public DecodedJWT validateToken(String token) {
        System.out.println("Validando token...");

        if (token == null || token.trim().isEmpty()) {
            System.out.println("Advertencia: El token es nulo o vacío.");
            throw new IllegalArgumentException("El token no puede estar vacío");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);
            System.out.println("Token válido.");
            return decodedJWT;

        } catch (JWTVerificationException ex) {
            System.out.println("Error al validar el token: " + ex.getMessage());
            return null;
        }
    }

    public String extractUsername(DecodedJWT decodedJWT) {
        System.out.println("Extrayendo nombre de usuario...");

        if (decodedJWT == null) {
            System.out.println("Advertencia: DecodedJWT es nulo.");
            return null;
        }

        String username = decodedJWT.getSubject();
        System.out.println("Nombre de usuario extraído: " + username);
        return username;
    }

    public Long extractUserId(DecodedJWT decodedJWT) {
        System.out.println("Extrayendo ID de usuario...");

        if (decodedJWT == null) {
            System.out.println("Advertencia: DecodedJWT es nulo.");
            return null;
        }

        Long userId = decodedJWT.getClaim("userId").asLong();
        System.out.println("ID de usuario extraído: " + userId);
        return userId;
    }

    public Map<String, Claim> getAllClaims(DecodedJWT decodedJWT) {
        System.out.println("Obteniendo todos los claims...");

        if (decodedJWT == null) {
            System.out.println("Advertencia: DecodedJWT es nulo.");
            return null;
        }

        Map<String, Claim> claims = decodedJWT.getClaims();
        System.out.println("Claims obtenidos con éxito.");
        return claims;
    }
}
