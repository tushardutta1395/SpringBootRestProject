package org.studyeasy.SpringRestDemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private RSAKey rsaKey;

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        rsaKey = Jwks.generateRsa();
        final var jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @Bean
    // public InMemoryUserDetailsManager users() {
    // return new InMemoryUserDetailsManager(
    // User.withUsername("tushar")
    // .password("{noop}password")
    // .authorities("read")
    // .build());
    // }

    @Bean
    public AuthenticationManager authManager(final UserDetailsService userDetailsService) {
        final var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }

    @Bean
    public JwtEncoder jwtEncoder(final JWKSource<SecurityContext> jwks) {
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        // httpSecurity
        // .authorizeHttpRequests()
        // .requestMatchers("/token").permitAll()
        // .requestMatchers("/").permitAll()
        // .requestMatchers("/swagger-ui/**").permitAll()
        // .requestMatchers("/v3/api-docs/**").permitAll()
        // .requestMatchers("/test").authenticated()
        // .and()
        // .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
        // .sessionManagement(session ->
        // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity
                // .csrf(csrf -> csrf.ignoringRequestMatchers("/db-console/**"))
                .headers(headers -> headers.frameOptions(options -> options.sameOrigin()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/api/v1/auth/token").permitAll()
                        .requestMatchers("/api/v1/auth/users/add").permitAll()
                        .requestMatchers("/api/v1/auth/users").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/v1/auth/users/{user_id}/update-authorities").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/v1/auth/profile").authenticated()
                        .requestMatchers("/api/v1/auth/profile/update-password").authenticated()
                        .requestMatchers("/api/v1/auth/profile/delete").authenticated()
                        // .requestMatchers("/").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        // .requestMatchers("/db-console/**").permitAll()
                        .requestMatchers("/test").authenticated())
                // .oauth2ResourceServer(OAuth2ResourceServerConfigurer :: jwt)
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // TODO: remove these after upgrading the DB from H2 infile DB
        // httpSecurity.csrf().disable();
        // httpSecurity.headers().frameOptions().disable();

        httpSecurity.csrf(csrf -> csrf.disable());
        httpSecurity.headers(headers -> headers.frameOptions(options -> options.disable()));

        return httpSecurity.build();
    }
}
