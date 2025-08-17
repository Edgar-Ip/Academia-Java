package com.project.Project_SpringBatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


/**
 * Clase principal de la aplicación Spring Boot
 * Aplicación para migración de customers de MySQL a MongoDB usando Spring Batch
 */

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.project.Project_SpringBatch.Repository")
@EnableJpaRepositories(basePackages = "com.project.Project_SpringBatch.Repository")
@Slf4j // Lombok: genera logger estático
public class ProjectSpringBatchClientesApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProjectSpringBatchClientesApplication.class, args);
	}

}
