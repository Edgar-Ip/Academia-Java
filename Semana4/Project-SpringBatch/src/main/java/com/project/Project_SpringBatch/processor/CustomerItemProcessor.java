package com.project.Project_SpringBatch.processor;


import com.project.Project_SpringBatch.Repository.CustomerDocumentRepository;
import com.project.Project_SpringBatch.domain.Customer;
import com.project.Project_SpringBatch.domain.CustomerDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Procesador que convierte Customer (MySQL) a CustomerDocument (MongoDB)
 * El ItemProcessor es responsable de:
 * - Transformar los datos del formato origen al formato destino
 * - Aplicar las reglas del negocio
 * - Filtrar registros (retornando null para omitir un registro)
 * - Enriquecer los datos con iinformación adicional
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerItemProcessor implements ItemProcessor<Customer, CustomerDocument> {

    private final CustomerDocumentRepository customerDocumentRepository;

    /**
     * Procesa cada Customer leído desde MySQL y lo convierte a CustomerDocument
     * Aplica validaciones y transformaciones necesarias
     *
     * @param customer Customer de MySQL a procesar
     * @return CustomerDocument para MongoDB, o null si debe ser omitido
     * @throws Exception Si ocurre durante el procesamiento
     */
    @Override
    public CustomerDocument process(Customer customer) throws Exception{

        //Log del customer que se está procesando
        log.debug("Procesando customer con ID: {} y email: {}",
                customer.getId(), customer.getEmail());

        //Validación 1: Verificar que el customer no sea null
        if (customer == null){
            log.warn("Customer null encontrado, omitiendo....");
            return null; //null significa que este item será omitido
        }

        //Validación 2: Verificar que tenga email válido
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()){
            log.warn("Customer con ID {} no tiene email válido, omitiendo....", customer.getId());
            return null;
        }

        //Validación 3: Verificar email duplicado por email
        if (customerDocumentRepository.existsByEmail(customer.getEmail())){
            log.warn("Customer con email {} ya existe en MongoDB, omitiendo...", customer.getEmail());
            return null;
        }


        //Trnasformaciones de datos
        CustomerDocument customerDocument = new CustomerDocument();
        //Mapeo de campos básicos
        customerDocument.setOriginalMysqlId(customer.getId());
        customerDocument.setName(customer.getName());
        customerDocument.setLastName(customer.getLastName());

        //Mapeo de fechas
        customerDocument.setRegisteredAt(customer.getRegisteredAt());

        //Transformación 1: Normalizar email a minúsculas
        customerDocument.setEmail(customer.getEmail().toLowerCase().trim());

        //Transformación 2: Capitalizar nombre y apellido
        customerDocument.setName(capitalizeFirstLetter(customer.getName()));
        customerDocument.setLastName(capitalizeFirstLetter(customer.getLastName()));

        //Transformación 3: Normalizar país
        customerDocument.setCountry(capitalizeFirstLetter(customer.getCountry()));

        log.info("Customer procesado exitosamente - MySQL ID: {}, Email: {}",
                customer.getId(), customer.getEmail());

        return customerDocument;

    }

    /**
     * Capitaliza la primera letra de cada palabra
     * @param text Texto a capitalizar
     * @return Texto con primera letra de cada palabra en mayúscula
     */
    private String capitalizeFirstLetter(String text){
        if (text == null || text.trim().isEmpty()){
            return text;
        }
        String[] words = text.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0){
                result.append(" ");
            }
            if (words[i].length() > 0){
                result.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));
            }
        }
        return result.toString();
    }
}
