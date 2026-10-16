
package com.example.helloworld;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Hello", description = "Endpoints de exemplo")
public class HelloController {

    @GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Retorna uma mensagem de Hello World",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Sucesso",
                        content = @Content(schema = @Schema(implementation = HelloResponse.class))
                )
            }
    )
    public ResponseEntity<HelloResponse> hello() {
        return ResponseEntity.ok(HelloResponse.of("Hello, World!"));
    }
}
