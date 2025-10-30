package org.example.backendweride.platform.iam.infrastructure.auth.configuration;

import org.example.backendweride.platform.iam.infrastructure.auth.pipeline.BearerAuthRequestFilter;
import org.example.backendweride.platform.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import org.example.backendweride.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;

/*
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
    private final UserDetailsService userDetailsService;
    private final BearerTokenService tokenService;
    private final BCryptHashingService hashingService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public WebSecurityConfig(
            UserDetailsService userDetailsService,
            BearerTokenService bearerTokenService,
            BCryptHashingService hashingService,
            AuthenticationEntryPoint authenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.tokenService = bearerTokenService;
        this.hashingService = hashingService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public BearerAuthRequestFilter authorizationRequestFilter() {
        return new BearerAuthRequestFilter(tokenService, userDetailsService);
    }

    )

}

 */