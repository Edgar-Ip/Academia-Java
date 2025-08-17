package com.project.Project_SpringBatch.domain;


//Documento MongoDB que representa un customer (ItemWriter)

import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


//Esta clase mapea la colección 'customers' en MongoDB

@Data
@NoArgsConstructor
@Document(collection = "customers")
public class CustomerDocument {

    @Id
    private String id;     //MongoDB ObjectId (string)

    @Field("original_mysql_id")
    private Long originalMysqlId; //ID único de MySQL para mantener la referencia

    @Field("name")
    private String name;

    @Field("lastName")
    private String lastName;

    @Field("email")
    private String email;

    @Field("country")
    private String country;

    @Field("registered_at")
    private LocalDateTime registeredAt;

    @Field("migration_status")
    private String migrationStatus;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() { return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}

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

    public String getMigrationStatus() { return migrationStatus;}

    public void setMigrationStatus(String migrationStatus) { this.migrationStatus = migrationStatus; }

    public Long getOriginalMysqlId() { return originalMysqlId; }

    public void setOriginalMysqlId(Long originalMysqlId) { this.originalMysqlId = originalMysqlId; }

    public CustomerDocument(Long originalMysqlId, String name, String lastName,
                            String email, String country,
                            LocalDateTime registeredAt){
       this.originalMysqlId = originalMysqlId;
       this.name = name;
       this.lastName = lastName;
       this.email = email;
       this.country = country;
       this.registeredAt = registeredAt;
       this.migrationStatus = "MIGRATED";

    }

}
