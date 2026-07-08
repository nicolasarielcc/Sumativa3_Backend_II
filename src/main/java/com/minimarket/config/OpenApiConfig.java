package com.minimarket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minimarket API")
                        .description("API REST para la gestión de un Minimarket. Documentación completa de endpoints, modelos y ejemplos con OpenAPI y HATEOAS.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Desarrollo Backend II")
                                .email("contacto@minimarket.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
