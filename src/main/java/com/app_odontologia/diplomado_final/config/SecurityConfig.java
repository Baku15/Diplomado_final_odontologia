package com.app_odontologia.diplomado_final.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final com.app_odontologia.diplomado_final.security.ClinicFilter clinicFilter;

    public SecurityConfig(com.app_odontologia.diplomado_final.security.ClinicFilter clinicFilter) {
        this.clinicFilter = clinicFilter;
    }

    // ===== Converter tipado: devuelve EXACTAMENTE Collection<GrantedAuthority> =====
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return (Jwt jwt) -> {
            var roles = jwt.getClaimAsStringList("roles"); // ["SUPERUSER", ...]
            Collection<GrantedAuthority> auths = new ArrayList<>();
            if (roles != null) {
                for (String r : roles) {
                    String roleName = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                    auths.add(new SimpleGrantedAuthority(roleName));
                }
            }
            return auths;
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurity(HttpSecurity http,
                                           JwtAuthenticationConverter jwtAuthConverter) throws Exception {

        http
                // Esta cadena SOLO aplica a /api/** y /actuator/**
                .securityMatcher("/api/**", "/actuator/**")

                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // API REST: sin login HTML
                .formLogin(c -> c.disable())
                .logout(c -> c.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔓 ENDPOINT DE FOTO PÚBLICO (para que <img> funcione sin token)
                        .requestMatchers(HttpMethod.GET, "/api/clinic/*/patients/*/photo").permitAll()

                        // 🔹 TODOS los endpoints públicos bajo /api/public/**
                        .requestMatchers("/api/public/**").permitAll()

                        // 🔐 ACTIVACIÓN
                        .requestMatchers(HttpMethod.POST, "/api/auth/activate/**").permitAll()

                        // Admin protegido por rol
                        .requestMatchers("/api/admin/**").hasRole("SUPERUSER")

                        // Cualquier otro /api/** requiere JWT
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthConverter)
                ))

                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"unauthorized\"}");
                }));

        http.addFilterAfter(clinicFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(99)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults())
                .requestCache(requestCache -> requestCache.requestCache(new CustomRequestCache()));

        return http.build();
    }

    // CORS para Angular en dev
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","X-Requested-With"));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
