package ca.gbc.comp3095.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        // Allow Swagger UI access through gateway for all services
                        .pathMatchers("/api/resources/swagger-ui.html", "/api/resources/swagger-ui/**", "/api/resources/api-docs/**", "/api/resources/v3/api-docs/**").permitAll()
                        .pathMatchers("/api/goals/swagger-ui.html", "/api/goals/swagger-ui/**", "/api/goals/api-docs/**", "/api/goals/v3/api-docs/**").permitAll()
                        .pathMatchers("/api/events/swagger-ui.html", "/api/events/swagger-ui/**", "/api/events/api-docs/**", "/api/events/v3/api-docs/**").permitAll()
                        .pathMatchers("/api/resources/webjars/**", "/api/goals/webjars/**", "/api/events/webjars/**").permitAll()

                        .pathMatchers("/api/goals/**").hasRole("student")
                        .pathMatchers("/api/events/*/register").hasRole("student")
                        .pathMatchers("/api/events/*/unregister").hasRole("student")
                        .pathMatchers("/api/events/**").hasAnyRole("student", "staff")

                        .pathMatchers("/api/resources/**").hasRole("staff")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                        )
                );

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Use container name for JWK set (reachable from Docker network)
        // But accept tokens with localhost:8090 as issuer (from browser)
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        // Accept issuer with wellness-hub realm regardless of host/port
        decoder.setJwtValidator(jwt -> {
            String issuer = jwt.getIssuer().toString();
            if (issuer.contains("/realms/wellness-hub")) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                new org.springframework.security.oauth2.core.OAuth2Error("invalid_token", 
                    "Invalid issuer: " + issuer, null));
        });
        return decoder;
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Try to get roles from realm_access
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // Fallback: try to get roles directly
            List<String> roles = jwt.getClaim("roles");
            if (roles != null) {
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            return List.of();
        }
    }
}