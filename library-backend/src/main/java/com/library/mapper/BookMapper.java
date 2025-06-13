package com.library.mapper;

import com.library.dto.*;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.BookAuthor;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, PublisherMapper.class, AuthorMapper.class})
public interface BookMapper {
    
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "publisher.id", target = "publisherId")
    @Mapping(source = "publisher.name", target = "publisherName")
    @Mapping(target = "authors", expression = "java(mapAuthorsToString(book))")
    BookDTO toDTO(Book book);
    
    @Mapping(source = "category", target = "category")
    @Mapping(source = "publisher", target = "publisher")
    @Mapping(target = "authors", expression = "java(mapBookAuthorsToAuthorDTOs(book))")
    @Mapping(target = "totalLoans", ignore = true)
    @Mapping(target = "currentLoans", ignore = true)
    @Mapping(target = "totalSales", ignore = true)
    BookDetailDTO toDetailDTO(Book book);
    
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "publisherId", target = "publisher.id")
    @Mapping(target = "bookAuthors", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Book toEntity(CreateBookRequestDTO dto);
    
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "publisherId", target = "publisher.id")
    @Mapping(target = "bookAuthors", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(UpdateBookRequestDTO dto, @MappingTarget Book book);
    
    List<BookDTO> toDTOList(List<Book> books);
    
    List<BookDetailDTO> toDetailDTOList(List<Book> books);
    
    @Named("mapAuthorsToString")
    default String mapAuthorsToString(Book book) {
        if (book.getBookAuthors() == null || book.getBookAuthors().isEmpty()) {
            return "";
        }
        return book.getBookAuthors().stream()
                .map(ba -> ba.getAuthor().getName())
                .sorted()
                .collect(Collectors.joining(", "));
    }
    
    @Named("mapBookAuthorsToAuthorDTOs")
    default List<AuthorDTO> mapBookAuthorsToAuthorDTOs(Book book) {
        if (book.getBookAuthors() == null || book.getBookAuthors().isEmpty()) {
            return List.of();
        }
        return book.getBookAuthors().stream()
                .map(BookAuthor::getAuthor)
                .map(author -> AuthorDTO.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .biography(author.getBiography())
                        .birthDate(author.getBirthDate())
                        .deathDate(author.getDeathDate())
                        .nationality(author.getNationality())
                        .website(author.getWebsite())
                        .build())
                .collect(Collectors.toList());
    }
}