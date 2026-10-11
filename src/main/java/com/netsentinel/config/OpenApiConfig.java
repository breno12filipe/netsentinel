package com.netsentinel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI netSentinelOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NetSentinel API")
                        .description("API de Monitoramento Integrado de Saúde de Serviços e Infraestrutura (L3 PING, L4 TCP Sockets, L7 HTTP)\"")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Suporte NetSentinel - Breno Filipe")
                                .email("brenofilipe1212@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));

    }
}
