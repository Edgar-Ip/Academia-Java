package com.project.Project_SpringBatch.tests;

import com.project.Project_SpringBatch.service.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Clase de test para BatchService
 * Utiliza Mockito para mockear las dependencias de Spring Batch
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para BatchService")
class BatchServiceTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job customerMigrationJob;

    @InjectMocks
    private BatchService batchService;

    private JobExecution jobExecution;
    private JobInstance jobInstance;
    private StepExecution stepExecution;

    @BeforeEach
    void setUp() {
        // Configurar JobInstance mock
        jobInstance = new JobInstance(1L, "customerMigrationJob");

        // Configurar JobParameters
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("execution.timestamp", "20240115-103000-123")
                .addString("execution.user", "batch-system")
                .toJobParameters();

        // Configurar JobExecution mock
        jobExecution = new JobExecution(jobInstance, 123L, jobParameters);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);

        // Configurar StepExecution mock
        stepExecution = new StepExecution("customerMigrationStep", jobExecution);
        stepExecution.setStatus(BatchStatus.COMPLETED);
        stepExecution.setReadCount(1000);
        stepExecution.setWriteCount(950);
        stepExecution.setCommitCount(100);
        stepExecution.setRollbackCount(2);
        stepExecution.setReadSkipCount(25);
        stepExecution.setProcessSkipCount(15);
        stepExecution.setWriteSkipCount(10);
        stepExecution.setFilterCount(50);

        // Agregar el step al job execution
        jobExecution.addStepExecutions(java.util.List.of(stepExecution));
    }

    @Test
    @DisplayName("runCustomerMigrationJob - Ejecución exitosa")
    void runCustomerMigrationJob_Success() throws Exception {
        // Given - Configurar mocks
        when(jobLauncher.run(eq(customerMigrationJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // When - Ejecutar el método
        JobExecution result = batchService.runCustomerMigrationJob();

        // Then - Verificar resultados
        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals("customerMigrationJob", result.getJobInstance().getJobName());
        assertEquals(BatchStatus.COMPLETED, result.getStatus());

        // Verificar que se llamó al jobLauncher
        verify(jobLauncher, times(1)).run(eq(customerMigrationJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("runCustomerMigrationJob - Job ya en ejecución")
    void runCustomerMigrationJob_AlreadyRunning() throws Exception {
        // Given - Simular que el job ya está corriendo
        when(jobLauncher.run(eq(customerMigrationJob), any(JobParameters.class)))
                .thenThrow(new JobExecutionAlreadyRunningException("Job is already running"));

        // When & Then - Verificar que se lanza RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchService.runCustomerMigrationJob();
        });

        assertEquals("Job migration is already running", exception.getMessage());
        verify(jobLauncher, times(1)).run(eq(customerMigrationJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("runCustomerMigrationJob - Error de reinicio")
    void runCustomerMigrationJob_RestartError() throws Exception {
        // Given - Simular error de reinicio
        when(jobLauncher.run(eq(customerMigrationJob), any(JobParameters.class)))
                .thenThrow(new JobRestartException("Cannot restart completed job"));

        // When & Then - Verificar que se lanza RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchService.runCustomerMigrationJob();
        });

        assertEquals("Failed to restart job", exception.getMessage());
        verify(jobLauncher, times(1)).run(eq(customerMigrationJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("runCustomerMigrationJob - Instancia ya completada")
    void runCustomerMigrationJob_InstanceAlreadyComplete() throws Exception {
        // Given - Simular instancia ya completada
        when(jobLauncher.run(eq(customerMigrationJob), any(JobParameters.class)))
                .thenThrow(new JobInstanceAlreadyCompleteException("Job instance already completed"));

        // When & Then - Verificar que se lanza RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchService.runCustomerMigrationJob();
        });

        assertEquals("Job instance already completed with same parameters", exception.getMessage());
        verify(jobLauncher, times(1)).run(eq(customerMigrationJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("runCustomerMigrationJob - Parámetros inválidos")
    void runCustomerMigrationJob_InvalidParameters() throws Exception {
        // Given - Simular parámetros inválidos
        when(jobLauncher.run(eq(customerMigrationJob), any(JobParameters.class)))
                .thenThrow(new JobParametersInvalidException("Invalid job parameters"));

        // When & Then - Verificar que se lanza RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchService.runCustomerMigrationJob();
        });

        assertEquals("Invalid job parameters", exception.getMessage());
        verify(jobLauncher, times(1)).run(eq(customerMigrationJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("getJobExecutionStatus - Obtener estado exitosamente")
    void getJobExecutionStatus_Success() {
        // When - Ejecutar el método
        Map<String, Object> result = batchService.getJobExecutionStatus(123L);

        // Then - Verificar resultados
        assertNotNull(result);
        assertEquals(123L, result.get("jobExecutionId"));
        assertTrue(result.containsKey("message"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    @DisplayName("getDetailedJobExecutionInfo - Información detallada")
    void getDetailedJobExecutionInfo_Success() {
        // When - Ejecutar el método
        Map<String, Object> result = batchService.getDetailedJobExecutionInfo(jobExecution);

        // Then - Verificar información básica
        assertNotNull(result);
        assertEquals(123L, result.get("jobExecutionId"));
        assertEquals(1L, result.get("jobInstanceId"));
        assertEquals("customerMigrationJob", result.get("jobName"));
        assertEquals("COMPLETED", result.get("status"));
        assertEquals("COMPLETED", result.get("exitStatus"));

        // Verificar información de steps
        assertTrue(result.containsKey("steps"));
        @SuppressWarnings("unchecked")
        Map<String, Object> stepsInfo = (Map<String, Object>) result.get("steps");
        assertTrue(stepsInfo.containsKey("customerMigrationStep"));

        @SuppressWarnings("unchecked")
        Map<String, Object> stepInfo = (Map<String, Object>) stepsInfo.get("customerMigrationStep");
        assertEquals("customerMigrationStep", stepInfo.get("stepName"));
        assertEquals("COMPLETED", stepInfo.get("status"));
        assertEquals(1000, stepInfo.get("readCount"));
        assertEquals(950, stepInfo.get("writeCount"));
        assertEquals(100, stepInfo.get("commitCount"));

        // Verificar parámetros del job
        assertTrue(result.containsKey("jobParameters"));
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) result.get("jobParameters");
        assertEquals("batch-system", parameters.get("execution.user"));
    }

    @Test
    @DisplayName("isMigrationJobRunning - Verificar estado de ejecución")
    void isMigrationJobRunning_Success() {
        // When - Ejecutar el método
        boolean result = batchService.isMigrationJobRunning();

        // Then - Verificar resultado (siempre "false" en la implementación actual)
        assertFalse(result);
    }

    @Test
    @DisplayName("getExecutiveSummary - Resumen ejecutivo exitoso")
    void getExecutiveSummary_Success() {
        // When - Ejecutar el método
        Map<String, Object> result = batchService.getExecutiveSummary(jobExecution);

        // Then - Verificar información básica del resumen
        assertNotNull(result);
        assertEquals("Customer Migration (MySQL → MongoDB)", result.get("jobName"));
        assertEquals(123L, result.get("executionId"));
        assertEquals("COMPLETED", result.get("status"));
        assertEquals(true, result.get("completed"));

        // Verificar estadísticas
        assertTrue(result.containsKey("statistics"));
        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
        assertEquals(1000, statistics.get("customersRead"));
        assertEquals(950, statistics.get("customersWritten"));
        assertEquals(50, statistics.get("customersSkipped")); // Sum of all skip counts
        assertEquals(2, statistics.get("errorsEncountered"));
        assertEquals(95.0, statistics.get("successRate")); // (950/1000) * 100

        // Verificar información de timing
        assertTrue(result.containsKey("startTime"));
        assertTrue(result.containsKey("endTime"));
        assertTrue(result.containsKey("durationMinutes"));
    }

    @Test
    @DisplayName("getExecutiveSummary - Job sin fechas")
    void getExecutiveSummary_NoTimestamps() {
        // Given - JobExecution sin fechas
        JobExecution jobWithoutDates = new JobExecution(jobInstance, 124L, new JobParameters());
        jobWithoutDates.setStatus(BatchStatus.STARTED);
        // No establecer startTime ni endTime

        // When - Ejecutar el método
        Map<String, Object> result = batchService.getExecutiveSummary(jobWithoutDates);

        // Then - Verificar que maneja correctamente la ausencia de fechas
        assertNotNull(result);
        assertEquals(124L, result.get("executionId"));
        assertEquals("STARTED", result.get("status"));
        assertEquals(false, result.get("completed"));
        assertFalse(result.containsKey("startTime"));
        assertFalse(result.containsKey("endTime"));
        assertFalse(result.containsKey("durationMinutes"));
    }

    @Test
    @DisplayName("getExecutiveSummary - Cálculo de estadísticas con múltiples steps")
    void getExecutiveSummary_MultipleSteps() {
        // Given - JobExecution con múltiples steps
        StepExecution step2 = new StepExecution("step2", jobExecution);
        step2.setReadCount(500);
        step2.setWriteCount(480);
        step2.setReadSkipCount(10);
        step2.setProcessSkipCount(5);
        step2.setWriteSkipCount(5);
        step2.setRollbackCount(1);

        jobExecution.addStepExecutions(java.util.List.of(step2));

        // When - Ejecutar el método
        Map<String, Object> result = batchService.getExecutiveSummary(jobExecution);

        // Then - Verificar estadísticas agregadas
        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
        assertEquals(1500, statistics.get("customersRead")); // 1000 + 500
        assertEquals(1430, statistics.get("customersWritten")); // 950 + 480
        assertEquals(70, statistics.get("customersSkipped")); // 50 + 20
        assertEquals(3, statistics.get("errorsEncountered")); // 2 + 1
        assertEquals(95.33333333333333, statistics.get("successRate")); // (1430/1500) * 100
    }
}
