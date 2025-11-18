package com.app_odontologia.diplomado_final.config;

import java.time.Duration;
import java.util.stream.Collectors;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {


    // 🔹 1️⃣ Cadena del Authorization Server
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var asConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        RequestMatcher asEndpoints = asConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(asEndpoints)
                .with(asConfigurer, config -> config.oidc(Customizer.withDefaults()))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(asEndpoints))
                .exceptionHandling(e -> e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

// Dentro de AuthorizationServerConfig

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        var tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(true)
                .build();

        var clientSettingsSpa = ClientSettings.builder()
                .requireProofKey(true) // PKCE obligatorio para el SPA
                .requireAuthorizationConsent(false)
                .build();

        var angularSpa = RegisteredClient.withId("spa-odontoweb")
                .clientId("odontoweb")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4200/callback")
                .postLogoutRedirectUri("http://localhost:4200")
                .scope("openid")
                .scope("profile")
                .scope("api.read")
                .scope("offline_access")
                .clientSettings(clientSettingsSpa)
                .tokenSettings(tokenSettings)
                .build();

        // CLIENTE CONFIDENCIAL para llamadas server-side (revocación)
        var backendClientSettings = ClientSettings.builder()
                .requireProofKey(false)
                .requireAuthorizationConsent(false)
                .build();

        var backendClient = RegisteredClient.withId("backend-client")
                .clientId("odontoweb-backend")
                // la secret real la guardaremos en application.properties y aquí la colocamos "en claro"
                // Spring maneja el encoding con PasswordEncoder si es necesario. (Puedes usar same encoder)
                .clientSecret(passwordEncoder().encode("b4ck3nd-s3cr3t"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // para poder autenticarse al revocation endpoint
                .scope("api.read")
                .clientSettings(backendClientSettings)
                .tokenSettings(tokenSettings)
                .build();

        return new InMemoryRegisteredClientRepository(angularSpa, backendClient);
    }


    // 🔹 JWT Decoder (valida los tokens firmados por este AS)
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // 🔹 Configuración del AS (issuer URL)
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }

    // 🔹 Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔹 Llave JWK RSA en memoria (para firmar tokens)
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsa = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsa);
        return (selector, ctx) -> selector.select(jwkSet);
    }

    // 🔹 Customizer: agrega “roles” y “username” al access token
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            Authentication auth = context.getPrincipal();
            if (auth == null) return;

            var roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)       // "ROLE_SUPERUSER"
                    .map(a -> a.replaceFirst("^ROLE_", ""))    // -> "SUPERUSER"
                    .collect(Collectors.toList());

            // Añadimos las claims tanto al access_token como al id_token
            String tokenType = context.getTokenType().getValue(); // e.g. "access_token" o "id_token"
            if ("access_token".equals(tokenType) || "id_token".equals(tokenType)) {
                context.getClaims()
                        .claim("roles", roles)
                        .claim("username", auth.getName());

                // Si tu principal es la entidad User, añade user_id / clinic_id también
                Object principal = auth.getPrincipal();
                if (principal instanceof com.app_odontologia.diplomado_final.model.entity.User) {
                    var u = (com.app_odontologia.diplomado_final.model.entity.User) principal;
                    if (u.getId() != null) context.getClaims().claim("user_id", u.getId());
                    if (u.getClinic() != null && u.getClinic().getId() != null)
                        context.getClaims().claim("clinic_id", u.getClinic().getId());
                }
            }
        };
    }
}

