package com.project.crud.Service;

import com.project.crud.dto.PersonDto;

import java.util.List;

public interface IPersonService {
    PersonDto create(PersonDto dto);
    PersonDto getById(String id);
    List<PersonDto> getAll();
    List<PersonDto> getByPetType(String petType);
    PersonDto update(String id, PersonDto dto);
    void delete(String id);
}
