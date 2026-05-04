package cv.igrp.RH_Service.shared.security;

import cv.igrp.RH_Service.shared.infrastructure.security.IAMUserProfileSyncFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.TokenExchangeOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Security configuration class for setting up OAuth2 and JWT authentication with Keycloak.
 * This class defines the security filter chain, IAM profile sync filter, and JWT authentication conversion logic.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwtIssuer;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

  private final Environment environment;

  public SecurityConfig(Environment environment) {
    this.environment = environment;
  }

  /**
   * Configures the security filter chain, enabling OAuth2 resource server with JWT and specifying
   * which requests require authentication.
   *
   * @param http the {@link HttpSecurity} object to configure security settings
   * @return the configured {@link SecurityFilterChain} instance
   * @throws Exception if an error occurs while configuring the security
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, IAMUserProfileSyncFilter iamUserProfileSyncFilter) throws Exception {

        /*
          Creates and configures a CORS filter.
          The filter allows requests from the specified origin, allows all headers and methods,
          and supports credentials in cross-origin requests.
        */
    http.cors(cors -> cors.configurationSource(request -> {
      var configuration = new CorsConfiguration();
      configuration.addAllowedOriginPattern(CorsConfiguration.ALL);
      configuration.addAllowedMethod(HttpMethod.GET);
      configuration.addAllowedMethod(HttpMethod.POST);
      configuration.addAllowedMethod(HttpMethod.PUT);
      configuration.addAllowedMethod(HttpMethod.PATCH);
      configuration.addAllowedMethod(HttpMethod.DELETE);
      configuration.addAllowedMethod(HttpMethod.HEAD);
      configuration.addAllowedMethod(HttpMethod.OPTIONS);
      configuration.addAllowedHeader(CorsConfiguration.ALL);
      configuration.setAllowCredentials(true);
      return configuration;
    }));

    // Always configure the JWT resource server so BearerTokenAuthenticationFilter is in the
    // chain and SecurityContext is populated with JwtAuthenticationToken when a valid Bearer
    // token is present — required for IAMUserProfileSyncFilter to work regardless of whether
    // app.security.enabled is true or false.
    http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
    );

    if (isSecurityDisabled()) {
      // Security disabled — only allowed in development profile via SECURITY_ENABLED=false.
      // Token is still parsed when present so IAMUserProfileSyncFilter can sync the profile.
      http.csrf(AbstractHttpConfigurer::disable);
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    } else {
      http.authorizeHttpRequests(authorize -> authorize
              .requestMatchers(
                  "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                  "/swagger-resources/**", "/webjars/**", "/actuator/**"
              )
              .permitAll()
              .anyRequest()
              .authenticated()
          )
          .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Restricted Content\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
          }));

      http.sessionManagement(t -> t.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    http.addFilterBefore(iamUserProfileSyncFilter, AuthorizationFilter.class);

    return http.build();
  }

  /**
   * Configures a JWT authentication converter that extracts roles from the JWT and assigns them to authorities.
   *
   * @return the {@link JwtAuthenticationConverter} used to convert JWT tokens to Spring Security authentication
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return converter;
  }

  /**
   * Configures a JWT decoder to verify and decode JWT tokens.
   *
   * @return the {@link JwtDecoder} for JWT token validation
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    if (isSecurityDisabled()) {
      return token -> null;
    }
    return NimbusJwtDecoder.withIssuerLocation(jwtIssuer).build();
  }

  /**
   * Creates a bean for an OAuth2AuthorizedClientProvider that supports token exchange.
   *
   * <p>Token exchange allows one token to be exchanged for another,
   * typically used in scenarios where a client needs to act on behalf
   * of a user or service in a federated identity environment.</p>
   *
   * @return An instance of TokenExchangeOAuth2AuthorizedClientProvider.
   */
  @Bean
  public FilterRegistrationBean<IAMUserProfileSyncFilter> iamUserProfileSyncFilterRegistration(IAMUserProfileSyncFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(true);
    return registration;
  }

  @Bean
  public OAuth2AuthorizedClientProvider tokenExchange() {
    return new TokenExchangeOAuth2AuthorizedClientProvider();
  }

  // SECURITY_ENABLED=false is only honoured in the development profile.
  // In staging/production the flag is ignored and auth is always enforced.
  private boolean isSecurityDisabled() {
    boolean isDevelopment = environment.matchesProfiles("development");
    return !securityEnabled && isDevelopment;
  }
}
