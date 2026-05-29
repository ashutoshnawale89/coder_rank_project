package com.code.rank.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI codeRankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodeRank API")
                        .description("Online code execution platform API")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste a token obtained from /api/auth/login")));
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder().group("Auth").pathsToMatch("/api/auth/**").build();
    }

    @Bean
    public GroupedOpenApi executionGroup() {
        return GroupedOpenApi.builder().group("Execution").pathsToMatch("/api/execute/**").build();
    }

    @Bean
    public GroupedOpenApi snippetsGroup() {
        return GroupedOpenApi.builder().group("Snippets").pathsToMatch("/api/snippets/**").build();
    }

    @Bean
    public GroupedOpenApi submissionsGroup() {
        return GroupedOpenApi.builder().group("Submissions").pathsToMatch("/api/submissions/**").build();
    }

    @Bean
    public GroupedOpenApi healthGroup() {
        return GroupedOpenApi.builder().group("Health").pathsToMatch("/api/health/**", "/api/health").build();
    }

    @Bean
    public GroupedOpenApi adminGroup() {
        return GroupedOpenApi.builder().group("Admin").pathsToMatch("/api/admin/**").build();
    }

    @Bean
    public GroupedOpenApi questionsGroup() {
        return GroupedOpenApi.builder().group("Questions").pathsToMatch("/api/questions/**").build();
    }

    @Bean
    public GroupedOpenApi solutionsGroup() {
        return GroupedOpenApi.builder().group("Solutions").pathsToMatch("/api/solutions/**").build();
    }
}
