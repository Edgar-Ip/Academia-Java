package com.project.crud.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "persons")
public class Person {


    @Id
    private String id;
    private String name;
    private Integer age;
    private String petType;
    private String email;

    public Person(){}

    public Person(String name, Integer age, String petType, String email){

        this.name = name;
        this.age = age;
        this.petType = petType;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return age;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public String getPetType() {
        return petType;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
