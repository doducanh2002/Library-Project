package com.library.mapper;

import com.library.dto.CreatePublisherRequestDTO;
import com.library.dto.PublisherDTO;
import com.library.dto.PublisherDetailDTO;
import com.library.dto.UpdatePublisherRequestDTO;
import com.library.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    
    @Mapping(target = "bookCount", ignore = true)
    @Mapping(target = "yearsInBusiness", ignore = true)
    PublisherDTO toDTO(Publisher publisher);
    
    @Mapping(target = "bookCount", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "yearsInBusiness", ignore = true)
    PublisherDetailDTO toDetailDTO(Publisher publisher);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "books", ignore = true)
    Publisher toEntity(CreatePublisherRequestDTO createRequest);
    
    List<PublisherDTO> toDTOList(List<Publisher> publishers);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateEntityFromDTO(UpdatePublisherRequestDTO updateRequest, @MappingTarget Publisher publisher);
}