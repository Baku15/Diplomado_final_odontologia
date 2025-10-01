package com.app_odontologia.diplomado_final.config;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    @Bean
    @Order(1) // la cadena del Authorization Server va primero
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var asConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        RequestMatcher endpoints = asConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpoints)
                .with(asConfigurer, authz -> authz.oidc(Customizer.withDefaults())) // habilita endpoints OIDC (opcional)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpoints))
                .exceptionHandling(e -> e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }


    // --- Clients registrados (tu SPA Angular con PKCE, sin secret) ---
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient angularSpa = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("odontoweb")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // SPA pública
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4200/callback")           // Angular
                .redirectUri("https://oauth.pstmn.io/v1/callback")       // ← Postman (añadir esta)
                .postLogoutRedirectUri("http://localhost:4200")
                .scope("openid")
                .scope("profile")
                .scope("api.read")
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true) // PKCE obligatorio
                                // .requireAuthorizationConsent(true) // opcional
                                .build()
                )
                .build();

        return new InMemoryRegisteredClientRepository(angularSpa);
    }

    // --- JWKSource (llave RSA en memoria para firmar tokens) ---
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();    // helper provisto abajo
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    // --- Decoder para validar los JWT emitidos por este AS ---
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // --- Ajustes del Authorization Server (define issuer explícito) ---
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080") // ajusta al host/puerto reales
                .build();
    }

    // --- Encoder para contraseñas de usuarios locales ---
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
