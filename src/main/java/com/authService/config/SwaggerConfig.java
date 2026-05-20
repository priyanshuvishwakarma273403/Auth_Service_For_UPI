package com.authService.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 Configuration
 * Swagger UI: http://localhost:8081/swagger-ui.html
 * API Docs:   http://localhost:8081/v3/api-docs
 */

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UPI Payment Mesh - Auth Service API")
                        .description("Authentication and Authorization microservice for UPI Payment Mesh System")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("UPI Mesh Team")
                                .email("dev@upimesh.com")))
                // JWT Bearer token Swagger UI mein support karo
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token enter karo: Bearer <token>")));
    }
}
