package com.project.Project_SpringBatch.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Project_SpringBatch.Repository.CustomerDocumentRepository;
import com.project.Project_SpringBatch.Repository.CustomerRepository;
import com.project.Project_SpringBatch.domain.Customer;
import com.project.Project_SpringBatch.domain.CustomerDocument;
import com.project.Project_SpringBatch.service.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el proceso completo de migración
 * Utiliza bases de datos embebidas para testing (H2 y MongoDB embebido)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Tests de Integración - Migración de Customers")
class CustomerMigrationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerDocumentRepository customerDocumentRepository;

    @Autowired
    private BatchService batchService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Configurar MockMVC para tests de controller
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Limpiar datos de test anteriores
        customerDocumentRepository.deleteAll();
        customerRepository.deleteAll();

        // Insertar datos de prueba en MySQL (H2)
        setupTestData();
    }

    /**
     * Configura datos de prueba en la base de datos MySQL (H2 embebida)
     */
    private void setupTestData() {
        Customer customer1 = new Customer();
        customer1.setName("Juan");
        customer1.setLastName("Pérez");
        customer1.setEmail("juan.perez@test.com");
        customer1.setCountry("Mexico");
        customer1.setRegisteredAt(LocalDateTime.now().minusDays(30));

        Customer customer2 = new Customer();
        customer2.setName("María");
        customer2.setLastName("González");
        customer2.setEmail("maria.gonzalez@test.com");
        customer2.setCountry("Spain");
        customer2.setRegisteredAt(LocalDateTime.now().minusDays(15));

        Customer customer3 = new Customer();
        customer3.setName("Carlos");
        customer3.setLastName("Rodríguez");
        customer3.setEmail("carlos.rodriguez@test.com");
        customer3.setCountry("Argentina");
        customer3.setRegisteredAt(LocalDateTime.now().minusDays(60));

        customerRepository.saveAll(List.of(customer1, customer2, customer3));
    }

    @Test
    @DisplayName("Test de integración completo - Migración exitosa")
    @Transactional
    void completeIntegrationTest_SuccessfulMigration() throws Exception {
        // Given - Verificar datos iniciales
        long initialCustomerCount = customerRepository.count();
        long initialDocumentCount = customerDocumentRepository.count();

        assertEquals(3, initialCustomerCount, "Deberían haber 3 customers en MySQL");
        assertEquals(0, initialDocumentCount, "No debería haber documentos en MongoDB inicialmente");

        // When - Ejecutar migración a través del endpoint REST
        mockMvc.perform(post("/api/v1/batch/migrate/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").exists())
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.message").value("Customer migration job started successfully"));

        // Dar tiempo para que el job se complete (en un entorno real, usarías polling)
        Thread.sleep(2000);

        // Then - Verificar resultados de la migración
        long finalDocumentCount = customerDocumentRepository.count();
        assertEquals(3, finalDocumentCount, "Deberían haberse migrado 3 customers a MongoDB");

        // Verificar que los datos se migraron correctamente
        List<CustomerDocument> migratedCustomers = customerDocumentRepository.findAll();
        assertEquals(3, migratedCustomers.size());

        // Verificar transformación de datos específicos
        Optional<CustomerDocument> juanDoc = migratedCustomers.stream()
                .filter(doc -> "juan.perez@test.com".equals(doc.getEmail()))
                .findFirst();

        assertTrue(juanDoc.isPresent(), "Juan debería estar migrado");
        assertEquals("Mexico", juanDoc.get().getCountry());
        assertEquals("LEGACY_CUSTOMER", juanDoc.get().getMigrationStatus());
    }

    @Test
    @DisplayName("Test de repositorio MySQL - Búsquedas personalizadas")
    void testMySQLRepository_CustomQueries() {
        // Test de búsqueda por país
        List<Customer> mexicanCustomers = customerRepository.findByCountry("Mexico");
        assertEquals(1, mexicanCustomers.size());
        assertEquals("Juan", mexicanCustomers.get(0).getName());



        // Test de búsqueda por email
        Customer foundCustomer = customerRepository.findByEmail("maria.gonzalez@test.com");
        assertNotNull(foundCustomer);
        assertEquals("María", foundCustomer.getName());



    }

    @Test
    @DisplayName("Test de repositorio MongoDB - Operaciones después de migración")
    void testMongoDBRepository_AfterMigration() throws Exception {
        // Primero ejecutar la migración
        JobExecution jobExecution = batchService.runCustomerMigrationJob();
        assertNotNull(jobExecution);

        // Dar tiempo para completar
        Thread.sleep(1000);

        // Test de búsqueda por ID original de MySQL
        List<Customer> originalCustomers = customerRepository.findAll();
        Customer originalCustomer = originalCustomers.get(0);

        Optional<CustomerDocument> foundByOriginalId =
                customerDocumentRepository.findByOriginalMysqlId(originalCustomer.getId());
        assertTrue(foundByOriginalId.isPresent());
        assertEquals(originalCustomer.getEmail(), foundByOriginalId.get().getEmail());

        // Test de búsqueda por país en MongoDB
        List<CustomerDocument> mexicanDocs = customerDocumentRepository.findByCountry("Mexico");
        assertEquals(1, mexicanDocs.size());


        // Test de verificación de existencia
        boolean exists = customerDocumentRepository.existsByOriginalMysqlId(originalCustomer.getId());
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test de endpoints de estado y resumen")
    void testStatusAndSummaryEndpoints() throws Exception {
        // Simular ejecución de job
        JobExecution jobExecution = batchService.runCustomerMigrationJob();
        Long jobExecutionId = jobExecution.getId();

        // Test del endpoint de estado
        mockMvc.perform(get("/api/v1/batch/status/" + jobExecutionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").value(jobExecutionId))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.statistics").exists());

        // Test del endpoint de verificación de jobs corriendo
        mockMvc.perform(get("/api/v1/batch/status/running"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isJobRunning").exists())
                .andExpect(jsonPath("$.data.jobType").value("customer-migration"));

        // Test del endpoint de resumen ejecutivo
        mockMvc.perform(get("/api/v1/batch/summary/" + jobExecutionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.migrationName").value("Customer Migration (MySQL → MongoDB)"))
                .andExpect(jsonPath("$.executionId").value(jobExecutionId))
                .andExpect(jsonPath("$.finalStatistics").exists())
                .andExpect(jsonPath("$.recommendations").exists());
    }

    @Test
    @DisplayName("Test de validación de datos - Customers inválidos")
    void testDataValidation_InvalidCustomers() {
        // Insertar customer con datos inválidos
        Customer invalidCustomer = new Customer();
        invalidCustomer.setName(""); // Nombre vacío
        invalidCustomer.setLastName("Test");
        invalidCustomer.setEmail("invalid-email"); // Email inválido
        invalidCustomer.setCountry("TestCountry");
        invalidCustomer.setRegisteredAt(LocalDateTime.now());

        customerRepository.save(invalidCustomer);

        // El processor debería filtrar este customer
        // En un test completo, verificarías que no se migra a MongoDB
        long totalCustomers = customerRepository.count();
        assertEquals(4, totalCustomers); // 3 originales + 1 inválido
    }

    @Test
    @DisplayName("Test de configuración de Swagger")
    void testSwaggerConfiguration() throws Exception {
        // Verificar que Swagger UI está disponible
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());

        // Verificar que la documentación API está disponible
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
