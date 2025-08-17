package com.project.Project_SpringBatch.reader;


import com.project.Project_SpringBatch.domain.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Comfiguración del ItemReader para leer customers desde MySQL
 * Utiliza JpaPaginItemReader para leer de forma paginada y eficiemte
 */
@Configuration
@RequiredArgsConstructor
@Slf4j //Lombok: Genera logger estático
public class CustomerItemReader {

    private final DataSource dataSource;

    /**
     * Configura y retorna un JdbCursorItemReader para leer customers desde MySQL
     * El cursor permite leer los datos de forma streaming sin cargar todo en memoria
     * @return JdbcCursorItemReader configuración
     */
    public JdbcCursorItemReader<Customer> customerReader(){
        log.info("Configurando JdbcItemReader"); //Nombre único para el reader

        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader") //Nombre único para el reader
                .dataSource(dataSource) //DataSource de MySQL configurado en properties
                .sql(buildSqlQuery()) //Query SQL para obtener los customers
                .rowMapper(this::mapRowToCustomer) //Mapper para convertir ResultSet a Customer
                .build();
    }

    /**
     * Construye la consulta SQL para obtener todos los customers
     * Se ordena por ID para asegurar un orden consistente en la lectura
     * @return String con la consulta SQL
     */
    private String buildSqlQuery(){
        return """
                SELECT
                    id, 
                    name, 
                    last_name,
                    email,
                    country,
                    registeredAt
                FROM customers
                ORDER BY id ASC
                """;
    }

    private Customer mapRowToCustomer(ResultSet rs, int rowNumber) throws SQLException{
        log.debug("Mapeando fila {} del Resultado a objeto Customer", rowNumber);

        Customer customer = new Customer();

        //Mapeo de campos básicos
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setCountry(rs.getString("country"));


        //Mapeo del campo fecha- Se convierte Timestamp a LocalDateTime
        if (rs.getTimestamp("registered_at") != null){
            customer.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
        }
        return customer;
    }
}
