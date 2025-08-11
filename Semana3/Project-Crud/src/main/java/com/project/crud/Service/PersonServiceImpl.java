package com.project.crud.Service;

import com.project.crud.Entity.Person;
import com.project.crud.Exception.ResourceNotFoundException;
import com.project.crud.Repository.IPersonRepository;
import com.project.crud.dto.PersonDto;
import com.project.crud.mapper.PersonMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonServiceImpl implements IPersonService {

    private  final IPersonRepository repository;
    private final PersonMapper mapper;

    public PersonServiceImpl(IPersonRepository repository, PersonMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PersonDto create(PersonDto dto){
        Person entity = mapper.toEntity(dto);
        Person saved = repository.save(entity);
        return  mapper.toDto(saved);
    }

    @Override
    public PersonDto getById(String id){
        Person p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
        return mapper.toDto(p);
    }


    @Override
    public List<PersonDto> getAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PersonDto> getByPetType(String petType) {
        return repository.findByPetTypeIgnoreCase(petType)
                .stream()
                .map(mapper::toDto)
                .toList();

    }

    @Override
    public PersonDto update(String id, PersonDto dto){
        Person existing = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Person not Found with id: " + id));
        existing.setName(dto.getName());
        existing.setAge(dto.getAge());
        existing.setPetType(dto.getPetType());
        existing.setEmail(dto.getEmail());
        Person updated = repository.save(existing);
        return mapper.toDto(updated);
    }

    public void delete(String id){
        if(!repository.existsById(id)){
            throw new ResourceNotFoundException("Person not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
