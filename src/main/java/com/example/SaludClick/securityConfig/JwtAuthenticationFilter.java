package com.example.SaludClick.securityConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = obtenerTokenDesdeHeader(request);

        if (token != null) {
            try {
                DecodedJWT decodedJWT = jwtUtils.validateToken(token);
                
                //Evita procesar un token inválido
                if (decodedJWT == null) {
                    logger.warn("Token inválido o expirado, acceso denegado.");
                    chain.doFilter(request, response);
                    return;
                }

                String username = jwtUtils.extractUsername(decodedJWT);

                //Verifica que el usuario exista en la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails == null) {
                    logger.warn("Usuario no encontrado: {}", username);
                    chain.doFilter(request, response);
                    return;
                }

                //Configurar la autenticación en el contexto de seguridad
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Autenticación establecida para el usuario: {}", username);
            } catch (Exception e) {
                logger.error("Error al validar el token: {}", e.getMessage());
            }
        } else {
            logger.warn("No se encontró un token en el encabezado.");
        }

        chain.doFilter(request, response);
    }

    private String obtenerTokenDesdeHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); //Quitamos Bearer 
        }
        return null;
    }
}
