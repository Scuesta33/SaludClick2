package com.example.SaludClick.securityConfig;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
        // Extraemos el token del encabezado Authorization
        String token = extractTokenFromHeader(request);

        // Verificamos que el token sea válido
        if (token != null && jwtUtils.validateTokenAndRetrieveSubject(token) != null) {
            // Si el token es válido, configuramos la autenticación en el contexto de seguridad
            String username = jwtUtils.validateTokenAndRetrieveSubject(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continuamos con el filtro
        chain.doFilter(request, response);
    }

    // Extraemos el token de la cabecera Authorization
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // El token sigue a "Bearer "
        }

        return null;
    }
}
