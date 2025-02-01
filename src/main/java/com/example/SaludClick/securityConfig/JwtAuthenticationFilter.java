package com.example.SaludClick.securityConfig;

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


    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        System.out.println("Revisando si hay token en la petición...");

        String token = obtenerTokenDesdeHeader(request);

        if (token != null) {
            System.out.println("Token encontrado en la cabecera, validando...");

            try {
                DecodedJWT decodedJWT = jwtUtils.validateToken(token);

                // Evita procesar un token inválido
                if (decodedJWT == null) {
                    System.out.println("Token inválido o expirado. No se puede continuar.");
                    chain.doFilter(request, response);
                    return;
                }

                String username = jwtUtils.extractUsername(decodedJWT);
                System.out.println("Usuario extraído del token: " + username);

                // Verifica que el usuario exista en la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (userDetails == null) { 
                    System.out.println("Advertencia: No se encontró el usuario en la base de datos.");
                    chain.doFilter(request, response);
                    return;
                }

                // Configurar la autenticación en el contexto de seguridad
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Autenticación exitosa para el usuario: " + username);
                
            } catch (Exception e) {
                System.out.println("Error al validar el token. ¿Está expirado? " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No se encontró un token en el encabezado.");
        }

        chain.doFilter(request, response);
    }

    private String obtenerTokenDesdeHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null) {
            if (authorizationHeader.startsWith("Bearer ")) {
                System.out.println("Token extraído del header.");
                return authorizationHeader.substring(7); // Quitamos "Bearer "
            } else {
                System.out.println("Advertencia: El token no tiene el prefijo esperado.");
            }
        } else {
            System.out.println("No hay cabecera de autorización.");
        }
        return null;
    }
}
