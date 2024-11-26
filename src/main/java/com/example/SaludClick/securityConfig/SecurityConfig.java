package com.example.SaludClick.securityConfig;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Configuración del SecurityFilterChain
    
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF con el nuevo formato
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(HttpMethod.POST, "/usuarios/registrar").permitAll() // Permite registro sin autenticación
	            .requestMatchers(HttpMethod.POST, "/usuarios/login").permitAll() // Permite login sin autenticación
	            .requestMatchers("/medico/**").hasRole("MEDICO")  // Médicos solo
	            .requestMatchers("/paciente/**").hasRole("PACIENTE")  // Pacientes solo
	            .anyRequest().authenticated() // Los demás endpoints requieren autenticación
	        )
	        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Añadimos el filtro JWT

	    return http.build(); // Usamos `http.build()` para construir la configuración
	}


    // Configuración del PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración del AuthenticationManager (si es necesario)
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }
}