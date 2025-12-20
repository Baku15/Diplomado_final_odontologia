package com.app_odontologia.diplomado_final.config;
import java.time.Duration;
import java.util.stream.Collectors;

import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    // NOTE: NO inyectar UserService ni cualquier bean que dependa de beans de este config
    // private final UserService userService;  <-- QUITADO

    // -----------------------------
    // Authorization Server filter chain
    // -----------------------------
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

    // -----------------------------
    // Use AuthenticationConfiguration to obtain AuthenticationManager (no UserService injected here)
    // -----------------------------
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // -----------------------------
    // Registered clients (SPA + backend)
    // -----------------------------
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

        var backendClientSettings = ClientSettings.builder()
                .requireProofKey(false)
                .requireAuthorizationConsent(false)
                .build();

        var backendClient = RegisteredClient.withId("backend-client")
                .clientId("odontoweb-backend")
                .clientSecret(passwordEncoder().encode("b4ck3nd-s3cr3t"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("api.read")
                .clientSettings(backendClientSettings)
                .tokenSettings(tokenSettings)
                .build();

        return new InMemoryRegisteredClientRepository(angularSpa, backendClient);
    }

    // -----------------------------
    // JWT Decoder (valida tokens firmados por este AS)
    // -----------------------------
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // -----------------------------
    // Authorization Server settings (issuer)
    // -----------------------------
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }

    // -----------------------------
    // Password encoder
    // -----------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -----------------------------
    // JWK Source (RSA key)
    // -----------------------------
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsa = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsa);
        return (selector, ctx) -> selector.select(jwkSet);
    }

    // -----------------------------
    // Token customizer: añade roles, username, clinic_id, user_id, mustCompleteProfile
    // -----------------------------
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(UserRepository userRepo) {
        return context -> {
            Authentication auth = context.getPrincipal();
            if (auth == null) return;

            var claims = context.getClaims();

            // Normalizar roles: quitar prefijo ROLE_ si existe
            var roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(a -> a.replaceFirst("^ROLE_", ""))
                    .distinct()
                    .collect(Collectors.toList());

            claims.claim("roles", roles);
            claims.claim("username", auth.getName());

            // Sólo intentamos buscar usuario si el principal parece una cuenta de persona
            // (no client_credentials). En client_credentials el nombre será el client_id.
            String principalName = auth.getName();
            if (principalName != null && !principalName.isBlank()) {
                try {
                    userRepo.findByUsername(principalName).ifPresent(u -> {
                        // Aseguramos tipos primitivos/compatibles en los claims
                        if (u.getId() != null) {
                            claims.claim("user_id", u.getId());
                        }
                        if (u.getClinic() != null && u.getClinic().getId() != null) {
                            claims.claim("clinic_id", u.getClinic().getId());
                        }
                        if (u.getMustCompleteProfile() != null) {
                            claims.claim("mustCompleteProfile", u.getMustCompleteProfile());
                        }
                    });
                } catch (Exception ex) {
                    // No lanzamos: si algo falla al consultar DB, dejamos emitir token sin esos claims
                    // (pero lo logueamos para debugging en desarrollo)
                    System.err.println("Warning: no se pudo añadir claims adicionales al token: " + ex.getMessage());
                }
            }

            // Nota: JwtEncodingContext se utiliza para id_token y access_token.
            // Añadir claims aquí los incluirá en ambos tipos cuando aplique.
        };
    }
}