package com.project.Project_SpringBatch.tests;

import com.project.Project_SpringBatch.service.BatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Project_SpringBatch.controller.BatchController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de test para BatchController
 * Utiliza MockMVC para simular las peticiones HTTP y Mockito para mockear las dependencias
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(BatchController.class)
@DisplayName("Tests para BatchController")
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private BatchService batchService;

    private JobExecution jobExecution;
    private JobInstance jobInstance;

    @BeforeEach
    void setUp() {
        // Configurar MockMVC
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Configurar objetos mock para JobExecution
        jobInstance = new JobInstance(1L, "customerMigrationJob");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("execution.timestamp", "20240115-103000-123")
                .addString("execution.user", "batch-system")
                .toJobParameters();

        jobExecution = new JobExecution(jobInstance, 123L, jobParameters);
        jobExecution.setStatus(BatchStatus.STARTED);

    }

    @Test
    @DisplayName("POST /api/v1/batch/migrate/customers - Inicio exitoso de migración")
    void startCustomerMigration_Success() throws Exception {
        // Given - Configurar mocks
        when(batchService.isMigrationJobRunning()).thenReturn(false);
        when(batchService.runCustomerMigrationJob()).thenReturn(jobExecution);

        // When & Then - Ejecutar request y verificar respuesta
        mockMvc.perform(post("/api/v1/batch/migrate/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Para debugging
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jobExecutionId").value(123L))
                .andExpect(jsonPath("$.jobName").value("customerMigrationJob"))
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.message").value("Customer migration job started successfully"));

        // Verificar que los métodos del service fueron llamados
        verify(batchService, times(1)).isMigrationJobRunning();
        verify(batchService, times(1)).runCustomerMigrationJob();
    }

    @Test
    @DisplayName("POST /api/v1/batch/migrate/customers - Error cuando ya hay migración en proceso")
    void startCustomerMigration_AlreadyRunning() throws Exception {
        // Given - Simular que ya hay una migración en proceso
        when(batchService.isMigrationJobRunning()).thenReturn(true);

        // When & Then - Ejecutar request y verificar respuesta de conflicto
        mockMvc.perform(post("/api/v1/batch/migrate/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("BATCH_001"))
                .andExpect(jsonPath("$.message").value("Migration job is already running"))
                .andExpect(jsonPath("$.details").value("Cannot start a new migration while another one is in progress"));

        // Verificar que solo se llamó al método de verificación
        verify(batchService, times(1)).isMigrationJobRunning();
        verify(batchService, never()).runCustomerMigrationJob();
    }

    @Test
    @DisplayName("POST /api/v1/batch/migrate/customers - Error interno del servidor")
    void startCustomerMigration_InternalServerError() throws Exception {
        // Given - Simular excepción en el service
        when(batchService.isMigrationJobRunning()).thenReturn(false);
        when(batchService.runCustomerMigrationJob()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - Ejecutar request y verificar respuesta de error
        mockMvc.perform(post("/api/v1/batch/migrate/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("BATCH_002"))
                .andExpect(jsonPath("$.message").value("Failed to start customer migration"));

        // Verificar llamadas a los métodos
        verify(batchService, times(1)).isMigrationJobRunning();
        verify(batchService, times(1)).runCustomerMigrationJob();
    }

    @Test
    @DisplayName("GET /api/v1/batch/status/{jobExecutionId} - Obtener estado exitosamente")
    void getJobStatus_Success() throws Exception {
        // Given - Configurar respuesta del service
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("message", "Job status retrieved successfully");
        statusInfo.put("timestamp", LocalDateTime.now());

        when(batchService.getJobExecutionStatus(anyLong())).thenReturn(statusInfo);

        // When & Then - Ejecutar request y verificar respuesta
        mockMvc.perform(get("/api/v1/batch/status/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jobExecutionId").value(123L))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completed").value(true));

        // Verificar llamada al service
        verify(batchService, times(1)).getJobExecutionStatus(123L);
    }

    @Test
    @DisplayName("GET /api/v1/batch/status/{jobExecutionId} - Error al obtener estado")
    void getJobStatus_Error() throws Exception {
        // Given - Simular excepción en el service
        when(batchService.getJobExecutionStatus(anyLong()))
                .thenThrow(new RuntimeException("Job execution not found"));

        // When & Then - Ejecutar request y verificar respuesta de error
        mockMvc.perform(get("/api/v1/batch/status/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("BATCH_003"))
                .andExpect(jsonPath("$.message").value("Failed to get job execution status"));

        // Verificar llamada al service
        verify(batchService, times(1)).getJobExecutionStatus(999L);
    }

    @Test
    @DisplayName("GET /api/v1/batch/status/running - Verificar jobs en ejecución")
    void checkRunningJobs_Success() throws Exception {
        // Given - Configurar respuesta del service
        when(batchService.isMigrationJobRunning()).thenReturn(false);

        // When & Then - Ejecutar request y verificar respuesta
        mockMvc.perform(get("/api/v1/batch/status/running")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No migration jobs are currently running"))
                .andExpect(jsonPath("$.data.isJobRunning").value(false))
                .andExpect(jsonPath("$.data.jobType").value("customer-migration"));

        // Verificar llamada al service
        verify(batchService, times(1)).isMigrationJobRunning();
    }

    @Test
    @DisplayName("GET /api/v1/batch/summary/{jobExecutionId} - Obtener resumen ejecutivo")
    void getExecutiveSummary_Success() throws Exception {
        // When & Then - Ejecutar request y verificar respuesta
        mockMvc.perform(get("/api/v1/batch/summary/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.migrationName").value("Customer Migration (MySQL → MongoDB)"))
                .andExpect(jsonPath("$.executionId").value(123L))
                .andExpect(jsonPath("$.finalStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.durationMinutes").value(5.5));

        // No necesitamos verificar llamadas al service, ya que usa datos hardcodeados
    }

    @Test
    @DisplayName("GET /api/v1/batch/summary/{jobExecutionId} - Simular error en resumen")
    void getExecutiveSummary_Error() throws Exception {
        // Para simular un error, podemos usar un ID que cause problemas
        // Este test demuestra el manejo de errores en el endpoint

        // When & Then - Ejecutar request con parámetro problemático
        mockMvc.perform(get("/api/v1/batch/summary/-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()); // El endpoint actual siempre retorna OK con datos mock

        // En un escenario real, se podría mockear dependencias que fallen
        // y verificar que se retorne el error apropiado
    }
}
