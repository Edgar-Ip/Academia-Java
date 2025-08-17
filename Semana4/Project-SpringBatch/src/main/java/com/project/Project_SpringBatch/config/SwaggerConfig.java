package com.project.Project_SpringBatch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API
 * Proporciona información detallada sobre los endpoints disponibles
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configura la documentación OpenAPI para la aplicación
     * @return instancia de OpenAPI configurada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServersList());
    }

    /**
     * Crea la información básica de la API
     * @return objeto Info con metadatos de la API
     */
    private Info createApiInfo() {
        return new Info()
                .title("Customer Migration API")
                .description("""
                    # Customer Migration API
                    
                    Esta API proporciona endpoints para gestionar la migración de customers desde MySQL hacia MongoDB 
                    utilizando Spring Batch.
                    
                    ## Características principales:
                    - **Migración por lotes**: Procesamiento eficiente de grandes volúmenes de datos
                    - **Monitoreo en tiempo real**: Seguimiento del progreso y estado de las migraciones
                    - **Gestión de errores**: Manejo robusto de errores y reintentos
                    - **Transformación de datos**: Conversión y enriquecimiento de datos durante la migración
                    
                    ## Flujo de trabajo típico:
                    1. **Iniciar migración**: `POST /api/v1/batch/migrate/customers`
                    2. **Monitorear progreso**: `GET /api/v1/batch/status/{jobExecutionId}`
                    3. **Obtener resumen**: `GET /api/v1/batch/summary/{jobExecutionId}`
                    
                    ## Tecnologías utilizadas:
                    - Spring Boot 3.1.5
                    - Spring Batch
                    - MySQL (origen)
                    - MongoDB (destino)
                    - Spring Data JPA
                    - Spring Data MongoDB
                    
                    ## Notas importantes:
                    - Los jobs se ejecutan de forma asíncrona
                    - Se mantiene un registro completo de todas las ejecuciones
                    - Los datos se procesan en chunks para optimizar el rendimiento
                    """)
                .version("1.0.0")
                .contact(createContactInfo())
                .license(createLicenseInfo());
    }

    /**
     * Crea la información de contacto
     * @return objeto Contact con información del equipo de desarrollo
     */
    private Contact createContactInfo() {
        return new Contact()
                .name("Development Team")
                .email("dev-team@example.com")
                .url("https://github.com/example/customer-migration");
    }

    /**
     * Crea la información de licencia
     * @return objeto License con información de la licencia
     */
    private License createLicenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Crea la lista de servidores disponibles
     * @return lista de servidores configurados
     */
    private List<Server> createServersList() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor de desarrollo local");

        Server productionServer = new Server()
                .url("https://api.example.com")
                .description("Servidor de producción");

        return List.of(localServer, productionServer);
    }
}
