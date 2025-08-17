package com.project.Project_SpringBatch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que maneja la ejecución de jobs de Spring Batch
 * Proporciona métodos para iniciar, monitorear y gestionar los procesos de migración
 */
@Service
@RequiredArgsConstructor
@Slf4j //Lombok: Genera logger estático
public class BatchService {
    private final JobLauncher jobLauncher;
    private final Job customerMigrationJob;


    /**
     * Ejecuta el job migración de customers de MySQL a MongoDB
     *
     * @return JobExecution cone l resultado de la ejecución
     * @throws Exception Sí ocurre algún error durante la ejecución
     */
    public JobExecution runCustomerMigrationJob() throws Exception {
        log.info("**** INICIANDO JOB DE MIGRACIÓN DE CUSTOMERS ****");

        try {
            //Crear parámetros únicos para el job para permitir múltiples ejecuciones
            JobParameters jobParameters = createJobParameters();

            log.info("Ejcutando job con parámetros: {}", jobParameters.getParameters());

            //Ejecutar el job
            JobExecution jobExecution = jobLauncher.run(customerMigrationJob, jobParameters);

            //Log del estado inicial
            log.info("Job iniciado exitosamente. JobExecutionId: {}, Status {}",
                    jobExecution.getJobId(), jobExecution.getStatus());

            return jobExecution;
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("El job ya está en ejecución: {}", e.getMessage());
            throw new RuntimeException("Job migration is already running", e);

        } catch (JobRestartException e) {
            log.error("Error al reiniciar el job: {}", e.getMessage());
            throw new RuntimeException("Failed to restar job", e);

        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Una instancia del job ya se completó con los mismos parámetros: {}", e.getMessage());
            throw new RuntimeException("Invalid job parameters", e);

        } catch (JobParametersInvalidException e) {
            log.error("Parámetros del job inválidos: {}", e.getMessage());
            throw new RuntimeException("Invalid job parameters", e);

        } catch (Exception e) {
            log.error("Error inesperado al ejecutar el job: {}", e.getMessage());
            throw new RuntimeException("Unexpected error running migration job", e);
        }
    }

    /**
     * Obtiene el estado actual de una ejecución de job
     *
     * @param jobExecutionId jobExcutionId ID de la ejecución del job
     * @return Map con información detallada del estado
     */
    public Map<String, Object> getJobExecutionStatus(Long jobExecutionId) {
        log.debug("Obteniendo estado del job execution ID: {}", jobExecutionId);

        Map<String, Object> status = new HashMap<>();
        try {
            //En un escenerio real, se obtendria la JobExecution desde el JobRepository
            //Por simplicidad, retornamos información básica
            status.put("JobExecutionId", jobExecutionId);
            status.put("message", "Use the JobExecution returned from runCustomerMigrationJob() for detailed status");
            status.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error al obtener estado del job execution {}: {}", jobExecutionId, e.getMessage());
            status.put("error", "Failed to get job execution status");
            status.put("errorMessage", e.getMessage());
        }
        return status;

    }

    /**
     * Crea un mapa con información detallada de la ejecución del job
     *
     * @param jobExecution ejecución del job
     * @return Map con información detallada
     */
    public Map<String, Object> getDetailedJobExecutionInfo(JobExecution jobExecution) {
        Map<String, Object> info = new HashMap<>();

        //Información básica del job
        info.put("jobExecutionId", jobExecution.getId());
        info.put("jobInstanceId", jobExecution.getJobInstance().getId());
        info.put("jobName", jobExecution.getJobInstance().getJobName());
        info.put("status", jobExecution.getStatus().toString());
        info.put("exitStatus", jobExecution.getExitStatus().getExitCode());

        //Tiempos de ejecución
        info.put("startTime", jobExecution.getStartTime());
        info.put("endTime", jobExecution.getEndTime());

        if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
            Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
            info.put("durationSeconds", duration.getSeconds());
        }

        // Información de los steps
        Map<String, Object> stepsInfo = new HashMap<>();
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            Map<String, Object> stepInfo = new HashMap<>();
            stepInfo.put("stepName", stepExecution.getStepName());
            stepInfo.put("status", stepExecution.getStatus().toString());
            stepInfo.put("readCount", stepExecution.getReadCount());
            stepInfo.put("writeCount", stepExecution.getWriteCount());
            stepInfo.put("commitCount", stepExecution.getCommitCount());
            stepInfo.put("rollbackCount", stepExecution.getRollbackCount());
            stepInfo.put("readSkipCount", stepExecution.getReadSkipCount());
            stepInfo.put("processSkipCount", stepExecution.getProcessSkipCount());
            stepInfo.put("writeSkipCount", stepExecution.getWriteSkipCount());
            stepInfo.put("filterCount", stepExecution.getFilterCount());

            stepsInfo.put(stepExecution.getStepName(), stepInfo);

        }

        info.put("steps", stepsInfo);

        //Parámetros del job
        Map<String, Object> parameters = new HashMap<>();
        jobExecution.getJobParameters().getParameters().forEach((key, value) ->
                parameters.put(key, value.getValue()));
        info.put("jobParameters", parameters);

        return info;
    }


    /**
     * Verifica si hay algún job de migración en ejecución
     * @return true si hay un job en ejecución, false en caso contrario
     */
    public boolean isMigrationJobRunning(){
        //En un escenario real, se consultaría el JobRepository para verificar
        //jobs en estado STARTED o STARTING
        log.debug("Verificando si hay jobs de migración en ejecución");

        //Por simplicidad, siempre retorna a false
        //En una implementación real, sería
        //return jobRepository.getLastJobExecution(jobName, jobParameters).getStatus() = BatchStatus.STARTED;

        return false;
    }

    /**
     * Crea parámetros únicos para cada ejecución del job
     * Esto permite ejecutar el mismo job múltiples veces
     * @return JobParameters con parámetros únicos
     */
    private JobParameters createJobParameters(){
        //Create timestamp único para permitir múltiples ejecuciones
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));

        JobParametersBuilder builder = new JobParametersBuilder();

        //Parámetro único basado en timestamp
        builder.addString("execution.timestamp", timestamp);

        //Parámetros adicionales que pueden ser útiles
        builder.addString("execution.user", "batch-system");
        builder.addString("migration.type", "mysql-to-mongodb");
        builder.addLong("execution.time", System.currentTimeMillis());

        JobParameters jobParameters = builder.toJobParameters();

        log.debug("Parámetros del job creados: timestamp = {}", timestamp);

        return jobParameters;
    }


    /**
     * Proporciona un resumen ejecutivo del job de migración
     * @param jobExecution ejecución del job
     * @return Map con resumen ejecutivo
     */
    public Map<String, Object> getExecutiveSummary(JobExecution jobExecution){
        Map<String, Object> summary = new HashMap<>();

        summary.put("jobName", "Customer Migration (MySQL -> MongoDB)");
        summary.put("executionId", jobExecution.getId());
        summary.put("status", jobExecution.getStatus().toString());
        summary.put("completed", jobExecution.getStatus() == BatchStatus.COMPLETED);


        //Calcular estadisticas agregadas de todos los steps
        int totalRead = 0;
        int totalWritten = 0;
        int totalSkipped = 0;
        int totalErrors = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()){
            totalRead += stepExecution.getReadCount();
            totalWritten += stepExecution.getWriteCount();
            totalSkipped += stepExecution.getReadSkipCount();
                            stepExecution.getProcessSkipCount();
                            stepExecution.getWriteSkipCount();
            totalErrors += stepExecution.getRollbackCount();
        }

        summary.put("statistics", Map.of(
                "customersRead", totalRead,
                "customerWritten", totalWritten,
                "customersSkipped", totalSkipped,
                "errorsEncountered", totalErrors,
                "successRate", totalRead > 0 ? (double) totalWritten / totalRead * 100 : 0
        ));

        //Información del timing
        if (jobExecution.getStartTime() != null){
            summary.put("startTime", jobExecution.getStartTime());
            if (jobExecution.getEndTime() != null){
                summary.put("endTime", jobExecution.getEndTime());
                Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
                summary.put("durationMinutes", duration.toMillis() / 60000.0);
            }
        }
        return summary;
    }
}