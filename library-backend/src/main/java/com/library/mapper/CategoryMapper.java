package com.library.mapper;

import com.library.dto.CategoryDTO;
import com.library.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "parentCategory.name", target = "parentCategoryName")
    CategoryDTO toDTO(Category category);
    
    @Mapping(source = "parentCategoryId", target = "parentCategory.id")
    Category toEntity(CategoryDTO categoryDTO);
    
    List<CategoryDTO> toDTOList(List<Category> categories);
    
    void updateEntityFromDTO(CategoryDTO categoryDTO, @MappingTarget Category category);
}