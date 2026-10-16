
package com.example.helloworld;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Hello API",
                version = "v1",
                description = "API de exemplo com Spring Boot 4.0 + springdoc-openapi"
        )
)
public class OpenApiConfig {}
