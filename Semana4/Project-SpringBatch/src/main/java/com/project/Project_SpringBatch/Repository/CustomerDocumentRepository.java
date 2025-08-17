package com.project.Project_SpringBatch.Repository;

import com.project.Project_SpringBatch.domain.CustomerDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para acceso a datos de CustomerDocument en MongoDB
 * Extiende MongoRepository para operaciones CRUD básicas
 */

@Repository
public interface CustomerDocumentRepository extends MongoRepository<CustomerDocument, String> {



    /**
     * Busca un customer document por su ID original de MySQL
     * @param originalMysqlId ID original de MySQL
     * @return Optional con el customer document encontrado
     */
    Optional<CustomerDocument> findByOriginalMysqlId(Long originalMysqlId);


    /**
     * Busca customer por país
     * @param country País a buscar
     * @return Lista de customer documents del país especificado
     */
    List<CustomerDocument> findByCountry(String country);

    /**
     * Verifica si ya existe un customer con el email dado
     * @param email Email a verificar
     * @return true si existe, false si no existe
     */
    boolean existsByEmail( String email);

    boolean existsByOriginalMysqlId(Long originalMysqlId);
}
