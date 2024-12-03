package com.example.SaludClick.securityConfig;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Extraer el token del encabezado Authorization
        String token = extractTokenFromHeader(request);

        if (token != null) {
            try {
                // Validar el token y obtener el JWT decodificado
                DecodedJWT decodedJWT = jwtUtils.validateToken(token);

                // Extraer el username (subject) del JWT
                String username = jwtUtils.extractUsername(decodedJWT);

                // Crear la autenticación y establecerla en el contexto de seguridad
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Si el token es inválido o ha expirado, se limpia el contexto de seguridad
                SecurityContextHolder.clearContext();
            }
        }

        // Continuar con el filtro
        chain.doFilter(request, response);
    }

    // Método para extraer el token de la cabecera Authorization
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Retornar el token sin el prefijo "Bearer "
        }

        return null;
    }
}