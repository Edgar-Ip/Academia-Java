package com.project.Project_SpringBatch.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

/**
 * Configuración de las bases de datos MySQL y MongoDB
 * Define las conexiones y configuraciones específicas para cada base de datos
 */
@Configuration
@EnableTransactionManagement // Habilita gestión de transacciones
@EnableJpaRepositories(
        basePackages = "com.example.repository.mysql", // Paquete para repositorios JPA/MySQL
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EnableMongoRepositories(
        basePackages = "com.example.repository.mongodb" // Paquete para repositorios MongoDB
)
@Slf4j // Lombok: genera logger estático
public class DatabaseConfig extends AbstractMongoClientConfiguration {

    // Configuraciones de MySQL desde application.properties
    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    // Configuraciones de MongoDB desde application.properties
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;

    /**
     * Configura el DataSource principal para MySQL
     * Este será usado por Spring Batch y los repositorios JPA
     * @return DataSource configurado para MySQL
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configurando DataSource principal para MySQL");
        log.debug("MySQL URL: {}", mysqlUrl);
        log.debug("MySQL Username: {}", mysqlUsername);

        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(mysqlUrl)
                    .username(mysqlUsername)
                    .password(mysqlPassword)
                    .driverClassName(mysqlDriverClassName)
                    .build();

            log.info("DataSource de MySQL configurado exitosamente");
            return dataSource;

        } catch (Exception e) {
            log.error("Error al configurar DataSource de MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure MySQL DataSource", e);
        }
    }

    /**
     * Configura el cliente MongoDB
     * @return MongoClient configurado
     */
    @Bean
    @Override
    public MongoClient mongoClient() {
        log.info("Configurando MongoClient");
        log.debug("MongoDB URI: {}", mongoUri);

        try {
            MongoClient mongoClient = MongoClients.create(mongoUri);
            log.info("MongoClient configurado exitosamente");
            return mongoClient;

        } catch (Exception e) {
            log.error("Error al configurar MongoClient: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure MongoDB client", e);
        }
    }

    /**
     * Retorna el nombre de la base de datos MongoDB
     * @return nombre de la base de datos MongoDB
     */
    @Override
    protected String getDatabaseName() {
        return mongoDatabaseName;
    }

    /**
     * Configura el MongoTemplate para operaciones MongoDB
     * @return MongoTemplate configurado
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        log.info("Configurando MongoTemplate");

        try {
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());
            log.info("MongoTemplate configurado exitosamente para base de datos: {}", getDatabaseName());
            return mongoTemplate;

        } catch (Exception e) {
            log.error("Error al configurar MongoTemplate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure MongoTemplate", e);
        }
    }

    /**
     * Configuración adicional para optimizar las conexiones MongoDB
     * @return configuración personalizada para MongoDB
     */
    @Override
    protected void configureClientSettings(com.mongodb.MongoClientSettings.Builder builder) {
        log.debug("Aplicando configuraciones personalizadas para MongoDB");


    }
}
