package fury.deep.project_builder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the config for swagger docs for the endpoints.
 *
 * @author night_fury_44
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI projectBuilderOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                .info(new Info()
                        .title("Project Builder API")
                        .description("Project management and analytics backend API")
                        .version("1.1.0")
                        .contact(new Contact()
                                .name("Deep")
                                .email("deep@example.com"))
                        .license(new License()
                                .name("MIT")));
    }
}

