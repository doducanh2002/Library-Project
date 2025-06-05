package com.library.mapper;

import com.library.dto.PublisherDTO;
import com.library.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    
    PublisherDTO toDTO(Publisher publisher);
    
    Publisher toEntity(PublisherDTO publisherDTO);
    
    List<PublisherDTO> toDTOList(List<Publisher> publishers);
    
    void updateEntityFromDTO(PublisherDTO publisherDTO, @MappingTarget Publisher publisher);
}