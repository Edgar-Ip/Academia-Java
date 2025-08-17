package com.project.Project_SpringBatch.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**Entidad JPA que representa la tabla 'customers' en MySQL
 * Se utilza esta clase como fuente de datos (Reader) em el proceso Spring Batch
 *
 * Caracteristicas Principales
 * - Mapeo directo con la tabla MySQL
 * - Documentación para Swagger
 */
@Entity
@Table(name= "customers")
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del cliente", example = "1")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @Schema(description = "nombre del cliente", example = "Bob")
    private String name;  //MySQL column: name

    @Column(name = "lastName", nullable = false, length = 100)
    @Schema(description = "apellido del cliente", example = "Johnson")
    private String lastName; //MySQL column: lastName;

    @Column(name = "email", nullable = false, length = 200)
    @Schema(description = "Email único del cliente", example = "juan.perez@example.com")
    private String email; //MySQL column: email

    @Column(name = "country", nullable = false, length = 100)
    @Schema(description = "País de residencia", example = "USA")
    private String country; //MySQL column: country

    @Column(name = "registered_at")
    @Schema(description = "Fecha de registro del cliente", example = "2024-01-15 10:30:00")
    private LocalDateTime registeredAt;

    public Customer(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() { return lastName;}

    public void setLastName(String lastName) { this.lastName = lastName;}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String toString(){
        return "Customer(id= " + id + ", name='" + name + "', email:'" + email + "'}";
    }
}
