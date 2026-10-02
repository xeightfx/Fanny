package com.fanny.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fanny.security.UsuarioDetailsService;

import java.util.List;

/**
 * Configuracion central de Spring Security.
 *
 * - BCrypt para las contraseñas.
 * - Autenticacion HTTP Basic (email + password en cada peticion). Es la forma
 *   mas simple de probar una API REST desde Postman/Insomnia o javascript (fetch con
 *   credentials). Mas adelante se puede reemplazar por JWT sin tocar los Controllers.
 * - Autorizacion por rutas: cada prefijo (/api/admin/**, /api/vendedor/**, etc.)
 *   exige el rol correspondiente. El backend valida esto SIEMPRE, sin importar
 *   lo que el frontend oculte o muestre.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authenticationProvider(authProvider)
            .authorizeHttpRequests(auth -> auth
                // catalogo publico: cualquiera puede mirar categorias y productos, sin login
                .requestMatchers(HttpMethod.GET, "/api/categorias/**", "/api/productos/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()

                // preguntar sobre un producto: cualquier usuario logueado (normalmente un cliente).
                // Esta regla va ANTES de las de admin/vendedor porque coincide con la misma URL
                // (/api/productos/**) y Spring Security usa la PRIMERA regla que haga match.
                .requestMatchers(HttpMethod.POST, "/api/productos/*/consultas").authenticated()

                // administrar categorias: solo ADMIN (seccion 1 del proyecto)
                .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")

                // cada actor solo entra a su prefijo (aqui viven los endpoints de administrar
                // productos del vendedor: /api/vendedor/productos/**)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/api/almacen/**").hasRole("ALMACEN")

                .anyRequest().authenticated())
            .httpBasic(basic -> {});

        return http.build();
    }

    // Permite que un frontend en otro origen (ej: Live Server en :5500) llame a esta API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
