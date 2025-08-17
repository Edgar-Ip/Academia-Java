package com.project.Project_SpringBatch.Repository;


import com.project.Project_SpringBatch.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para acceso de datos de Customer en MySQL
 * Extiende JpaRepository para operaciones CRUD básicas
 */

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    /**
     * Busca un customer por su email
     *
     * @param email Email del customer
     * @return Optional con el customer encontrado
     */

    Customer findByEmail(String email);


    /**
     * Busca customers por país
     *
     * @param country País a buscar
     * @return Lista de customers del país especificado
     */
    List<Customer> findByCountry(String country);

}
