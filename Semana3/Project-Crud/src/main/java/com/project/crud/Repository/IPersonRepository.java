package com.project.crud.Repository;

import com.project.crud.Entity.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IPersonRepository extends MongoRepository<Person, String> {
     List<Person> findByPetTypeIgnoreCase(String petType);
}
