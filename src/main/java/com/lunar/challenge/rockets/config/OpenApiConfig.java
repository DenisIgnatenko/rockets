package com.lunar.challenge.rockets.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rocketsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rockets API")
                        .description("API for tracking rocket states")
                        .version("v1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                        .contact(new Contact()
                                .name("Denis Ignatenko")
                                .email("denis.ignatenko.dev@gmail.com")
                                .url("https://github.com/DenisIgnatenko"))
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local environment"),
                        new Server().url("https://implement.later").description("Production environment")
                ))
                .tags(List.of(
                        new Tag().name("rockets").description("Operations about rockets"),
                        new Tag().name("messages").description("Receiving rocket messages")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Rockets Challenge Docs")
                        .url("https://github.com/DenisIgnatenko/rockets"));
    }
}