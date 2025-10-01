package com.app_odontologia.diplomado_final.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity // si usas @PreAuthorize en controladores/servicios
public class SecurityConfig {

    @Bean
    @Order(2) // queda detrás del AuthorizationServer (ORDER 1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // Ignoramos CSRF para que POST/PUT desde Postman/Angular no exijan token CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/registration").permitAll()
                        .requestMatchers("/api/registration/**").permitAll()
                        // Admin
                        .requestMatchers("/api/admin/**").hasRole("SUPERUSER")
                        // Resto protegido por Bearer JWT
                        .anyRequest().authenticated()
                )
                // Resource Server (validación de JWT emitidos por tu Authorization Server)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // Opcional (útil para probar login form de admin en /login):
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    // (Opcional) CORS para Angular en dev
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}
