package com.example.helloworld;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta padrão da API")
public record HelloResponse(

        @Schema(description = "Mensagem de resposta", example = "Hello, World!")
        String message,

        @Schema(description = "Nome da aplicação", example = "hello-world")
        String application,

        @Schema(description = "Versão da API", example = "v1")
        String version,

        @Schema(description = "Data e hora da resposta no formato ISO-8601")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public static HelloResponse of(String message) {
        return new HelloResponse(
                message,
                "hello-world",
                "v1",
                LocalDateTime.now()
        );
    }
}
