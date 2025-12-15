package com.company.orchestrator.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transferOrchestratorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transfer Orchestrator API")
                        .description("""
                    Policy-driven data transfer orchestration service.
                    
                    Features:
                    - Policy evaluation
                    - EDC contract negotiation
                    - Auditing & compliance
                    - Transfer lifecycle orchestration
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Niloy Datta")
                                .email("juniloydatta@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
