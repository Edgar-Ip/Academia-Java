package com.project.Project_SpringBatch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para las respuestas de los endpoints de Spring Batch
 */
public class BatchResponseDto {

    /**
     * DTO para la respuesta del inicio de migración
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta del inicio de migración de customers")
    public static class MigrationStartResponse {

        @Schema(description = "ID único de la ejecución del job", example = "12345")
        private Long jobExecutionId;

        @Schema(description = "Nombre del job ejecutado", example = "customerMigrationJob")
        private String jobName;

        @Schema(description = "Estado actual del job", example = "STARTED")
        private String status;

        @Schema(description = "Mensaje descriptivo del resultado", example = "Migration job started successfully")
        private String message;

        @Schema(description = "Fecha y hora de inicio", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @Schema(description = "Parámetros utilizados en la ejecución")
        private Map<String, Object> jobParameters;
    }


    /**
     * DTO para la respuesta del estado del job
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Estado detallado de la ejecución del job")
    public static class JobStatusResponse {
        @Schema(description = "ID de la ejecución del job", example = "12345")
        private Long jobExecutionId;

        @Schema(description = "Estado actual del job", example = "COMPLETED")
        private String status;

        @Schema(description = "Código de salida", example = "COMPLETED")
        private String exitCode;

        @Schema(description = "Indica si el job se completó exitosamente", example = "true")
        private Boolean completed;

        @Schema(description = "Fecha y hora de inicio", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @Schema(description = "Fecha y hora de finalización", example = "2024-01-15T10:35:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        @Schema(description = "Duración en segundos", example = "300")
        private Long durationSeconds;

        @Schema(description = "Estadísticas de procesamiento")
        private ProcessingStatistics statistics;

        @Schema(description = "Información detallada de los steps")
        private Map<String, Object> stepsInfo;
    }

    /**
     * DTO para estadísticas de procesamiento
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Estadísticas del procesamiento de datos")
    public static class ProcessingStatistics {

        @Schema(description = "Número de customers leídos desde MySQL", example = "1000")
        private Integer customersRead;

        @Schema(description = "Número de customers escritos en MongoDB", example = "950")
        private Integer customersWritten;

        @Schema(description = "Número de customers omitidos", example = "50")
        private Integer customersSkipped;

        @Schema(description = "Número de errores encontrados", example = "5")
        private Integer errorsEncountered;

        @Schema(description = "Tasa de éxito en porcentaje", example = "95.0")
        private Double successRate;

        @Schema(description = "Número de commits realizados", example = "100")
        private Integer commitCount;

        @Schema(description = "Número de rollbacks realizados", example = "2")
        private Integer rollbackCount;
    }

    /**
     * DTO para respuesta de error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta en caso de error")
    public static class ErrorResponse {

        @Schema(description = "Código de error", example = "BATCH_001")
        private String errorCode;

        @Schema(description = "Mensaje de error", example = "Job execution failed")
        private String message;

        @Schema(description = "Descripción detallada del error")
        private String details;

        @Schema(description = "Timestamp del error", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        @Schema(description = "Sugerencias para resolver el error")
        private String suggestions;
    }

    /**
     * DTO para respuesta genérica de éxito
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta genérica de éxito")
    public static class SuccessResponse {

        @Schema(description = "Indica si la operación fue exitosa", example = "true")
        private Boolean success;

        @Schema(description = "Mensaje descriptivo", example = "Operation completed successfully")
        private String message;

        @Schema(description = "Datos adicionales de la respuesta")
        private Map<String, Object> data;

        @Schema(description = "Timestamp de la respuesta", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
    }

    /**
     * DTO para el resumen ejecutivo de la migración
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen ejecutivo de la migración")
    public static class ExecutiveSummary {

        @Schema(description = "Nombre del proceso de migración")
        private String migrationName;

        @Schema(description = "ID de ejecución del job")
        private Long executionId;

        @Schema(description = "Estado final del proceso")
        private String finalStatus;

        @Schema(description = "Duración total en minutos", example = "5.5")
        private Double durationMinutes;

        @Schema(description = "Estadísticas finales del procesamiento")
        private ProcessingStatistics finalStatistics;

        @Schema(description = "Fecha y hora de inicio del proceso")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processStartTime;

        @Schema(description = "Fecha y hora de finalización del proceso")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processEndTime;

        @Schema(description = "Recomendaciones post-migración")
        private String recommendations;
    }
}


