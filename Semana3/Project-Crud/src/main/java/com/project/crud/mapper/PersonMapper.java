package com.project.crud.mapper;

import com.project.crud.Entity.Person;
import com.project.crud.dto.PersonDto;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {
    public Person toEntity(PersonDto dto){
        if (dto == null) return null;

        Person p = new Person();
        p.setId(dto.getId());
        p.setName(dto.getName());
        p.setAge(dto.getAge());
        p.setPetType(dto.getPetType());
        p.setEmail(dto.getEmail());
        return p;
    }

    public PersonDto toDto(Person p){
        if(p == null) return  null;

        PersonDto dto = new PersonDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setAge(p.getAge());
        dto.setPetType(p.getPetType());
        dto.setEmail(p.getEmail());

        return  dto;
    }
}
