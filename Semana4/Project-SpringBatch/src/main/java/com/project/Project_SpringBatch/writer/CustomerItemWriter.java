package com.project.Project_SpringBatch.writer;

import com.project.Project_SpringBatch.Repository.CustomerDocumentRepository;
import com.project.Project_SpringBatch.domain.CustomerDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * ItemWriter personalizado que escribe objetos CustomerDocument en MongoDB
 *
 * El ItemWriter es responsable de:
 * - Escribir los datos procesados en el destino final (MongoDB)
 * - Manejar operaciones en lotes para mejorar el rendimiento
 * - Gestionar duplicados y errores de escritura
 * - Aplicar la lógica de persistencia específica
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerItemWriter implements ItemWriter<CustomerDocument> {

    private final MongoTemplate mongoTemplate;
    private final CustomerDocumentRepository customerDocumentRepository;

    /**
     * Escribe un chunk (lote) de CustomerDocuments en MongoDB
     * Este método se ejecuta para cada chunk procesado por Spring Batch
     *
     * @param chunk lote de CustomerDocuments a escribir
     * @throws Exception si ocurre algún error durante la escritura
     */
    public void write(Chunk<? extends CustomerDocument> chunk) throws Exception {
        List<? extends CustomerDocument> customers = chunk.getItems();

        log.info("Iniciando escritura de {} customers en MongoDB", customers.size());

        if (customers.isEmpty()) {
            log.warn("Chunk vacío recibido. No hay datos para escribir");
            return;
        }

        //Lista para almacenar customers que realmente se van a insertar
        List<CustomerDocument> customersToInsert = new ArrayList<>();
        int duplicatesCount = 0;
        int errorsCount = 0;

        //Procesar cada customer del chunk
        for (CustomerDocument customer : customers) {
            try {
                //Verificar si ya existe un customer con el mismo ID de MySQL
                if (isDuplicate(customer)) {
                    log.debug("Customer con originalMysqlId {} ya existe. Se omitirá",
                            customer.getOriginalMysqlId());
                    duplicatesCount++;
                    continue;
                }
                //Validar customer antes de insertar
                if (isValidForInsertion(customer)) {
                    customersToInsert.add(customer);
                } else {
                    log.warn("Customer con originalMysqlId {} no es válido para inserción",
                            customer.getOriginalMysqlId());
                    errorsCount++;
                }

            } catch (Exception e) {
                log.error("Error al procesar customer con originMysqlId {}: {}",
                        customer.getOriginalMysqlId(), e.getMessage(), e);
                errorsCount++;

            }
        }

        //Realizar inserción en lote si hay customers válidos

        if (!customersToInsert.isEmpty()) {
            try {
                List<CustomerDocument> savedCustomers = customerDocumentRepository.saveAll(customersToInsert);
                log.info("Successfully wrote {} customers to MongoDB", savedCustomers.size());

                //Log detallado de customers guardados

                if (log.isDebugEnabled()) {
                    savedCustomers.forEach(customer ->
                            log.debug("Saved customer: ID={}, OriginiMysqlId={}, Email={}",
                                    customer.getId(), customer.getOriginalMysqlId(), customer.getEmail()));
                }
            } catch (Exception e) {
                log.error("Error al guardar customers en MongoDB: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to write customers to MongoDB", e);
            }
        }

        //Log del resumen de la operación
        logWriteSummary(customers.size(), customersToInsert.size(), duplicatesCount, errorsCount);
    }

    /**
     * Verificar si un customer ya existe en MongoDB
     *
     * @param customer customer a verificar
     * @return true si ya existe, falso en caso contrario
     */
    private boolean isDuplicate(CustomerDocument customer) {
        try {
            return customerDocumentRepository.existsByOriginalMysqlId(customer.getOriginalMysqlId());
        } catch (Exception e) {
            log.error("Error al verificar duplicado para originalMysqlId {}: {}",
                    customer.getOriginalMysqlId(), e.getMessage());

            //En caso de error, asumimos que no es duplicado para continuar el procesamiento
            return false;
        }
    }

    /**
     * Valida si un customer es válido para inserción en MongoDB
     *
     * @param customer customer a Validar
     * @return true si es válido, false en caso contrario
     */
    private boolean isValidForInsertion(CustomerDocument customer) {
        //Validar campos requeridos
        if (customer.getOriginalMysqlId() == null || customer.getOriginalMysqlId() <= 0) {
            log.warn("Customer sin originalMysqlId válido");
            return false;
        }

        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            log.warn("Customer con originalMysqlId {} sin name", customer.getOriginalMysqlId());
            return false;
        }

        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            log.warn("Customer con originalMysqlId {} sin lastName", customer.getOriginalMysqlId());
            return false;
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            log.warn("Customer con originalMysqlId {} sin email", customer.getOriginalMysqlId());
            return false;
        }

        if (customer.getCountry() == null || customer.getCountry().trim().isEmpty()) {
            log.warn("Customer con originalMysqlId {} sin country", customer.getOriginalMysqlId());
            return false;
        }
        return true;
    }

    /**
     * Registra un resumen detallado de la operación de escritura
     * @param totalReceived totalReceived total de customers recibidos
     * @param totalInserted totalInserted total de customers insertados
     * @param duplicatesCount duplicatesCount cantidad de duplicados omitidos
     * @param errorsCount cantidad de errores
     */
    private void logWriteSummary(int totalReceived, int totalInserted, int duplicatesCount, int errorsCount) {
        log.info("*** RESUMEN DE ESCRITURA");
        log.info("Total customers recibidos: {}", totalReceived);
        log.info("Total customers insertados: {}", totalInserted);
        log.info("Duplicados omitidos: {}", duplicatesCount);
        log.info("Errores encontrados: {}", errorsCount);
        log.info("*****************************");

        //Alertar si hay muchos errores
        if (errorsCount > 0) {
            double errorRate = (double) errorsCount / totalReceived * 100;
            if (errorRate > 10.0) { //Más del 10% de errores
                log.warn("ALTA TASA DE ERRORES: {:.2f}% de los registros tuvieron ERRORES", errorRate);
            }
        }
    }
}
