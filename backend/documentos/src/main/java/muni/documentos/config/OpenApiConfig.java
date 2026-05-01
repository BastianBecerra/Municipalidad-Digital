package muni.documentos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Documentos - Municipalidad Digital")
                        .version("1.0")
                        .description("Servicio para la generación, firma digital y gestión de documentos municipales (JJVV, Licitaciones, Contratos, Salvoconductos, Residencia)."));
    }
}
