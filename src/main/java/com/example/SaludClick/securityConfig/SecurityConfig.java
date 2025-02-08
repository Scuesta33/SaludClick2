package com.example.SaludClick.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// esta clase configura la seguridad de la aplicación
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    //configura las urls que requieren autenticación
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.POST, "/usuarios/registrar").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll();
                auth.requestMatchers("/paciente/**").hasRole("PACIENTE");
                auth.requestMatchers("/medico/**").hasRole("MEDICO");
                auth.requestMatchers("/citas/**").hasAnyRole("PACIENTE", "MEDICO");
                auth.requestMatchers("/usuarios/datos").hasAnyRole("PACIENTE", "MEDICO"); 
                auth.requestMatchers(HttpMethod.DELETE, "/usuarios/eliminar/**").authenticated();
                auth.requestMatchers(HttpMethod.POST, "/disponibilidad/crear").hasRole("MEDICO");
                auth.requestMatchers(HttpMethod.GET, "/disponibilidad/medico/**").hasRole("MEDICO");
                auth.requestMatchers(HttpMethod.DELETE, "/disponibilidad/{id}").hasRole("MEDICO");
                auth.requestMatchers("/citas/consultas").hasRole("MEDICO");
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        System.out.println("seguridad configurada.");
        return http.build();
    }

    //codifica las contraseñas con bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //el autenticationManeger se encarga de autenticar a los usuarios
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        System.out.println("configurando autenticación... :)");
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                                     .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    //cors se usa para conectar el backend con el frontend
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*"); 
        config.addAllowedMethod("*"); 
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*"); 
        config.addAllowedMethod("*"); 
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
