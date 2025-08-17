package com.project.Project_SpringBatch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .info(createApiInfo());    }

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
                    """);
            
    }

}

