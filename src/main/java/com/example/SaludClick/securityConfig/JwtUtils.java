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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${security.jwt.private.key}")
    private String secretKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator; // Generador del token (issuer)

    private static final long EXPIRATION_TIME_MS = 30 * 60 * 1000; // 30 minutos

    public String createToken(Authentication authentication, Long userId) {
        if (authentication == null || userId == null) {
            throw new IllegalArgumentException("Autenticación y UserID no pueden ser nulos");
        }

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        try {
            return JWT.create()
                    .withIssuer(userGenerator)
                    .withSubject(email)
                    .withClaim("roles", roles)
                    .withClaim("userId", userId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                    .withJWTId(UUID.randomUUID().toString())
                    .withNotBefore(new Date())
                    .sign(algorithm);
        } catch (Exception e) {
            logger.error("Error al generar el token JWT", e);
            throw new RuntimeException("No se pudo generar el token");
        }
    }

    public DecodedJWT validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("El token no puede estar vacío");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();

            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            logger.warn("Error al validar token: {}", ex.getMessage());
            return null;
        }
    }

    public String extractUsername(DecodedJWT decodedJWT) {
        if (decodedJWT == null) {
            logger.warn("DecodedJWT es nulo, no se puede extraer el nombre de usuario");
            return null;
        }
        return decodedJWT.getSubject();
    }

    public Long extractUserId(DecodedJWT decodedJWT) {
        if (decodedJWT == null) {
            logger.warn("DecodedJWT es nulo, no se puede extraer el ID de usuario");
            return null;
        }
        return decodedJWT.getClaim("userId").asLong();
    }

    public Map<String, Claim> getAllClaims(DecodedJWT decodedJWT) {
        if (decodedJWT == null) {
            logger.warn("DecodedJWT es nulo, no se pueden obtener los claims");
            return null;
        }
        return decodedJWT.getClaims();
    }
}
