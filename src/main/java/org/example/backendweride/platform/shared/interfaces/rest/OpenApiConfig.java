package org.example.backendweride.platform.shared.interfaces.rest;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds the "Authorize" button to Swagger UI so a JWT can be pasted once and
 * reused across every "Try it out" call — without this, every endpoint that
 * requires a token (all of them, since P-2) is untestable from the UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bearerAuthOpenAPI() {
        String schemeName = "bearerAuth";
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}
