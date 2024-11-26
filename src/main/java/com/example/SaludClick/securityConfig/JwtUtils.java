package com.example.SaludClick.securityConfig;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
@Component
public class JwtUtils {
    @Value("${SaludClick.app.jwtSecret}")
  private String secret;
  
  private long expirationMs;
  
  public String generateToken(String email) {
	  Date issuedAt = new Date();
	  Date expiresAt = new Date(issuedAt.getTime() + expirationMs);
	  return JWT.create()
               .withSubject(email)
               .withIssuedAt(issuedAt)
               .withExpiresAt(expiresAt)
               .sign(Algorithm.HMAC256(secret));
  }
  public String validateTokenAndRetrieveSubject(String token) {
	    try {
	        // Creamos el algoritmo HMAC256 con el secret
	        Algorithm algorithm = Algorithm.HMAC256(secret);

	        // Creamos el verifier con el algoritmo
	        JWTVerifier verifier = JWT.require(algorithm)
	                .build(); // Construimos el JWTVerifier

	        // Verificamos el token
	        DecodedJWT decodedJWT = verifier.verify(token);  // Verifica el token

	        // Extraemos el "subject" del JWT
	        return decodedJWT.getSubject();
	    } catch (Exception e) {
	        // En caso de cualquier error, lanzamos una excepción
	        throw new RuntimeException("Invalid token");
	    }
	}

  public boolean validateToken(String token) {
	  try {
		  getDecodedToken(token);
		  return true;
	  } catch (JWTVerificationException e) {
		  System.out.println("token invalidoo: " + e.getMessage());
		  return false;
	  }
  }
  
  public String getEmailFromToken(String token) {
	  return getDecodedToken(token).getSubject();
  }
  
  private DecodedJWT getDecodedToken(String token) {
	  JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
	  return verifier.verify(token);
  }
}