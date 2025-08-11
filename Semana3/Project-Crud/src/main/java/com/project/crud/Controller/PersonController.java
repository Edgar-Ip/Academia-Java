package com.project.crud.Controller;


import com.project.crud.Service.IPersonService;
import com.project.crud.dto.PersonDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/persons")
@Tag(name = "Person API", description = "API para gestión de personas")

public class PersonController {

    private final IPersonService service;

    public PersonController(IPersonService service){
        this.service = service;
    }

    @Operation(summary = "Crear una nueva persona",
            description = "Crea una nueva persona con los datos proporcionados")
    @ApiResponse(responseCode = "201", description = "Persona creada exitosamente")
    @PostMapping
    public ResponseEntity<PersonDto> create( @Parameter(description = "Datos de la persona a crear", required = true)
                                                 @Valid @RequestBody PersonDto dto){
        PersonDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/persons/" + created.getId())).body(created);
    }


    @Operation(summary = "Obtener persona por ID",
            description = "Recupera una persona usando su identificador único")
    @ApiResponse(responseCode = "200", description = "Persona encontrada")
    @ApiResponse(responseCode = "404", description = "Persona no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<PersonDto> getById(@Parameter(description = "ID de la persona", required = true)
                                                 @PathVariable String id){
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Listar personas",
            description = "Obtiene la lista completa de personas, o filtra por tipo de mascota si se proporciona")
    @ApiResponse(responseCode = "200", description = "Lista de personas obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<PersonDto>> getAll( @Parameter(description = "Filtrar por tipo de mascota")
                                                       @RequestParam(required = false) String petType){
        if (petType != null && !petType.isBlank()){
            return ResponseEntity.ok(service.getByPetType(petType));
        }
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Actualizar persona",
            description = "Actualiza los datos de una persona existente usando su ID")
    @ApiResponse(responseCode = "200", description = "Persona actualizada correctamente")
    @ApiResponse(responseCode = "404", description = "Persona no encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<PersonDto> update(@Parameter(description = "ID de la persona a actualizar", required = true)
                                                @PathVariable String id, @Parameter(description = "Datos actualizados de la persona", required = true)
    @Valid @RequestBody PersonDto dto){
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Eliminar persona",
            description = "Elimina una persona usando su ID")
    @ApiResponse(responseCode = "204", description = "Persona eliminada exitosamente")
    @ApiResponse(responseCode = "404", description = "Persona no encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID de la persona a eliminar", required = true)
                                           @PathVariable String id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
