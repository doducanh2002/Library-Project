package com.library.mapper;

import com.library.dto.AuthorDTO;
import com.library.dto.AuthorDetailDTO;
import com.library.dto.CreateAuthorRequestDTO;
import com.library.dto.UpdateAuthorRequestDTO;
import com.library.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    
    @Mapping(target = "bookCount", ignore = true)
    @Mapping(target = "age", ignore = true)
    @Mapping(target = "isAlive", ignore = true)
    AuthorDTO toDTO(Author author);
    
    @Mapping(target = "bookCount", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "age", ignore = true)
    @Mapping(target = "isAlive", ignore = true)
    AuthorDetailDTO toDetailDTO(Author author);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bookAuthors", ignore = true)
    Author toEntity(CreateAuthorRequestDTO createRequest);
    
    List<AuthorDTO> toDTOList(List<Author> authors);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bookAuthors", ignore = true)
    void updateEntityFromDTO(UpdateAuthorRequestDTO updateRequest, @MappingTarget Author author);
}