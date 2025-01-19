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
    private String privateKey; // Clave secreta para firmar el token

    @Value("${security.jwt.user.generator}")
    private String userGenerator; // Generador del token (issuer)

  
    public String createToken(Authentication authentication, Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(privateKey);

        // Aquí el principal ya debería ser el email, ya que es lo que usas para autenticar al usuario
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();  // Asegúrate de que esto es un String (correo)

        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(username)  // El 'subject' es el email.
                .withClaim("authorities", authorities)
                .withClaim("id", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1800000)) // Expira en 30 minutos
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis())) 
                .sign(algorithm);
    }
 
   
    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(privateKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Invalid token. Not authorized");
        }
    }

  
    public String extractUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }

   
     // Devuelve todos los claims presentes en un token JWT decodificado.
    
    public Map<String, Claim> returnAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }

    // Extrae el ID del usuario del token JWT decodificado.
     
    
    public Long extractUserId(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("id").asLong();
    }
}