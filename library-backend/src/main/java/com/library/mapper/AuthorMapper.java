package com.library.mapper;

import com.library.dto.AuthorDTO;
import com.library.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    
    AuthorDTO toDTO(Author author);
    
    Author toEntity(AuthorDTO authorDTO);
    
    List<AuthorDTO> toDTOList(List<Author> authors);
    
    List<Author> toEntityList(List<AuthorDTO> authorDTOs);
    
    void updateEntityFromDTO(AuthorDTO authorDTO, @MappingTarget Author author);
}