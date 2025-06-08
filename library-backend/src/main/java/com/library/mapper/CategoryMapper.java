package com.library.mapper;

import com.library.dto.CategoryDTO;
import com.library.dto.CategoryDetailDTO;
import com.library.dto.CreateCategoryRequestDTO;
import com.library.dto.UpdateCategoryRequestDTO;
import com.library.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "parentCategory.name", target = "parentCategoryName")
    @Mapping(target = "bookCount", ignore = true)
    CategoryDTO toDTO(Category category);
    
    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "parentCategory.name", target = "parentCategoryName")
    @Mapping(target = "bookCount", ignore = true)
    @Mapping(target = "totalBooksInSubcategories", ignore = true)
    CategoryDetailDTO toDetailDTO(Category category);
    
    @Mapping(source = "parentCategoryId", target = "parentCategory.id")
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toEntity(CreateCategoryRequestDTO createRequest);
    
    List<CategoryDTO> toDTOList(List<Category> categories);
    
    @Mapping(source = "parentCategoryId", target = "parentCategory.id")
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDTO(UpdateCategoryRequestDTO updateRequest, @MappingTarget Category category);
}