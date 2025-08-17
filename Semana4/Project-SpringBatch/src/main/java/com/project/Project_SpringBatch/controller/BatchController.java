package com.project.Project_SpringBatch.controller;

import com.project.Project_SpringBatch.dto.BatchResponseDto;
import com.project.Project_SpringBatch.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para gestionar los procesos de Spring Batch
 * Proporciona endpoints para iniciar, monitorear y gestionar las migraciones de customers
 */
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor // Lombok: genera constructor con campos final/required
@Slf4j // Lombok: genera logger estático
@Tag(name = "Batch Migration", description = "APIs para gestionar la migración de customers de MySQL a MongoDB")
public class BatchController {

    private final BatchService batchService;

    /**
     * Endpoint para iniciar la migración de customers de MySQL a MongoDB
     * @return ResponseEntity con información del job iniciado
     */
    @PostMapping("/migrate/customers")
    @Operation(
            summary = "Iniciar migración de customers",
            description = "Inicia el proceso de migración de customers desde MySQL hacia MongoDB usando Spring Batch"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Migración iniciada exitosamente",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.MigrationStartResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya hay una migración en proceso",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.ErrorResponse.class))
            )
    })
    public ResponseEntity<?> startCustomerMigration() {
        log.info("=== REQUEST: Iniciando migración de customers ===");

        try {
            // Verificar si ya hay una migración en proceso
            if (batchService.isMigrationJobRunning()) {
                log.warn("Intento de iniciar migración cuando ya hay una en proceso");

                BatchResponseDto.ErrorResponse errorResponse = BatchResponseDto.ErrorResponse.builder()
                        .errorCode("BATCH_001")
                        .message("Migration job is already running")
                        .details("Cannot start a new migration while another one is in progress")
                        .timestamp(LocalDateTime.now())
                        .suggestions("Wait for the current migration to complete or check its status")
                        .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Ejecutar el job de migración
            JobExecution jobExecution = batchService.runCustomerMigrationJob();

            // Preparar respuesta exitosa
            BatchResponseDto.MigrationStartResponse response = BatchResponseDto.MigrationStartResponse.builder()
                    .jobExecutionId(jobExecution.getId())
                    .jobName(jobExecution.getJobInstance().getJobName())
                    .status(jobExecution.getStatus().toString())
                    .message("Customer migration job started successfully")
                    .jobParameters(extractJobParameters(jobExecution))
                    .build();

            log.info("Migración iniciada exitosamente. JobExecutionId: {}", jobExecution.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al iniciar migración de customers: {}", e.getMessage(), e);

            BatchResponseDto.ErrorResponse errorResponse = BatchResponseDto.ErrorResponse.builder()
                    .errorCode("BATCH_002")
                    .message("Failed to start customer migration")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .suggestions("Check server logs and database connectivity")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener el estado de una ejecución específica del job
     * @param jobExecutionId ID de la ejecución del job
     * @return ResponseEntity con el estado detallado del job
     */
    @GetMapping("/status/{jobExecutionId}")
    @Operation(
            summary = "Obtener estado del job",
            description = "Obtiene el estado detallado de una ejecución específica del job de migración"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.JobStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job execution no encontrado",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.ErrorResponse.class))
            )
    })
    public ResponseEntity<?> getJobStatus(
            @Parameter(description = "ID de la ejecución del job", example = "12345")
            @PathVariable Long jobExecutionId) {

        log.info("=== REQUEST: Obteniendo estado del job execution ID: {} ===", jobExecutionId);

        try {
            Map<String, Object> statusInfo = batchService.getJobExecutionStatus(jobExecutionId);

            // En un escenario real, aquí construirías la respuesta desde la JobExecution real
            BatchResponseDto.JobStatusResponse response = BatchResponseDto.JobStatusResponse.builder()
                    .jobExecutionId(jobExecutionId)
                    .status("COMPLETED") // Placeholder - usar statusInfo real
                    .exitCode("COMPLETED")
                    .completed(true)
                    .startTime(LocalDateTime.now().minusMinutes(5))
                    .endTime(LocalDateTime.now())
                    .durationSeconds(300L)
                    .statistics(createSampleStatistics())
                    .stepsInfo(statusInfo)
                    .build();

            log.info("Estado del job obtenido exitosamente para ID: {}", jobExecutionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener estado del job {}: {}", jobExecutionId, e.getMessage(), e);

            BatchResponseDto.ErrorResponse errorResponse = BatchResponseDto.ErrorResponse.builder()
                    .errorCode("BATCH_003")
                    .message("Failed to get job execution status")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .suggestions("Verify the job execution ID is valid")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para verificar si hay algún job de migración en ejecución
     * @return ResponseEntity indicando si hay jobs en ejecución
     */
    @GetMapping("/status/running")
    @Operation(
            summary = "Verificar jobs en ejecución",
            description = "Verifica si hay algún job de migración actualmente en ejecución"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.SuccessResponse.class))
            )
    })
    public ResponseEntity<BatchResponseDto.SuccessResponse> checkRunningJobs() {
        log.info("=== REQUEST: Verificando jobs en ejecución ===");

        boolean isRunning = batchService.isMigrationJobRunning();

        Map<String, Object> data = new HashMap<>();
        data.put("isJobRunning", isRunning);
        data.put("jobType", "customer-migration");

        BatchResponseDto.SuccessResponse response = BatchResponseDto.SuccessResponse.builder()
                .success(true)
                .message(isRunning ? "There are migration jobs currently running" : "No migration jobs are currently running")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Verificación de jobs completada. Jobs en ejecución: {}", isRunning);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener un resumen ejecutivo de la migración
     * @param jobExecutionId ID de la ejecución del job
     * @return ResponseEntity con resumen ejecutivo
     */
    @GetMapping("/summary/{jobExecutionId}")
    @Operation(
            summary = "Obtener resumen ejecutivo",
            description = "Obtiene un resumen ejecutivo de la migración con métricas principales"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.ExecutiveSummary.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job execution no encontrado",
                    content = @Content(schema = @Schema(implementation = BatchResponseDto.ErrorResponse.class))
            )
    })
    public ResponseEntity<?> getExecutiveSummary(
            @Parameter(description = "ID de la ejecución del job", example = "12345")
            @PathVariable Long jobExecutionId) {

        log.info("=== REQUEST: Obteniendo resumen ejecutivo para job ID: {} ===", jobExecutionId);

        try {
            // En un escenario real, obtendrías la JobExecution real
            BatchResponseDto.ExecutiveSummary summary = BatchResponseDto.ExecutiveSummary.builder()
                    .migrationName("Customer Migration (MySQL → MongoDB)")
                    .executionId(jobExecutionId)
                    .finalStatus("COMPLETED")
                    .durationMinutes(5.5)
                    .finalStatistics(createSampleStatistics())
                    .processStartTime(LocalDateTime.now().minusMinutes(6))
                    .processEndTime(LocalDateTime.now().minusMinutes(1))
                    .recommendations("Migration completed successfully. Consider running data validation checks.")
                    .build();

            log.info("Resumen ejecutivo generado para job ID: {}", jobExecutionId);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("Error al generar resumen ejecutivo para job {}: {}", jobExecutionId, e.getMessage(), e);

            BatchResponseDto.ErrorResponse errorResponse = BatchResponseDto.ErrorResponse.builder()
                    .errorCode("BATCH_004")
                    .message("Failed to generate executive summary")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .suggestions("Verify the job execution ID and try again")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Extrae los parámetros del job como un Map
     * @param jobExecution ejecución del job
     * @return Map con los parámetros del job
     */
    private Map<String, Object> extractJobParameters(JobExecution jobExecution) {
        Map<String, Object> parameters = new HashMap<>();
        jobExecution.getJobParameters().getParameters().forEach((key, value) ->
                parameters.put(key, value.getValue()));
        return parameters;
    }

    /**
     * Crea estadísticas de ejemplo para demostración
     * En un escenario real, estas vendrían de la JobExecution real
     * @return ProcessingStatistics con datos de ejemplo
     */
    private BatchResponseDto.ProcessingStatistics createSampleStatistics() {
        return BatchResponseDto.ProcessingStatistics.builder()
                .customersRead(1000)
                .customersWritten(950)
                .customersSkipped(50)
                .errorsEncountered(5)
                .successRate(95.0)
                .commitCount(100)
                .rollbackCount(2)
                .build();
    }
}