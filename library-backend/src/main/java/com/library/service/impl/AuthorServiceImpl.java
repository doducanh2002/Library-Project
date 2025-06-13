package com.library.service.impl;

import com.library.dto.AuthorDTO;
import com.library.dto.AuthorDetailDTO;
import com.library.dto.BookDTO;
import com.library.dto.CreateAuthorRequestDTO;
import com.library.dto.UpdateAuthorRequestDTO;
import com.library.entity.Author;
import com.library.entity.BookAuthor;
import com.library.exception.BookNotFoundException;
import com.library.mapper.AuthorMapper;
import com.library.mapper.BookMapper;
import com.library.repository.AuthorRepository;
import com.library.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    
    @Override
    @Transactional
    public AuthorDetailDTO createAuthor(CreateAuthorRequestDTO createRequest) {
        log.info("Creating new author with name: {}", createRequest.getName());
        
        // Validate death date if provided
        if (createRequest.getDeathDate() != null && createRequest.getBirthDate() != null &&
            createRequest.getDeathDate().isBefore(createRequest.getBirthDate())) {
            throw new IllegalArgumentException("Death date cannot be before birth date");
        }
        
        Author author = authorMapper.toEntity(createRequest);
        author = authorRepository.save(author);
        
        log.info("Successfully created author with id: {}", author.getId());
        return enrichAuthorDetailDTO(authorMapper.toDetailDTO(author));
    }
    
    @Override
    public AuthorDetailDTO getAuthorById(Long id) {
        log.debug("Fetching author by id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Author not found with id: " + id));
        
        return enrichAuthorDetailDTO(authorMapper.toDetailDTO(author));
    }
    
    @Override
    public AuthorDetailDTO getAuthorWithBooks(Long id) {
        log.debug("Fetching author with books by id: {}", id);
        Author author = authorRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new BookNotFoundException("Author not found with id: " + id));
        
        AuthorDetailDTO dto = enrichAuthorDetailDTO(authorMapper.toDetailDTO(author));
        
        // Map books
        List<BookDTO> books = author.getBookAuthors().stream()
                .map(BookAuthor::getBook)
                .map(bookMapper::toDTO)
                .toList();
        dto.setBooks(books);
        
        return dto;
    }
    
    @Override
    @Transactional
    public AuthorDetailDTO updateAuthor(Long id, UpdateAuthorRequestDTO updateRequest) {
        log.info("Updating author with id: {}", id);
        
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Author not found with id: " + id));
        
        // Validate death date if provided
        LocalDate birthDate = updateRequest.getBirthDate() != null ? updateRequest.getBirthDate() : author.getBirthDate();
        LocalDate deathDate = updateRequest.getDeathDate() != null ? updateRequest.getDeathDate() : author.getDeathDate();
        
        if (deathDate != null && birthDate != null && deathDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("Death date cannot be before birth date");
        }
        
        authorMapper.updateEntityFromDTO(updateRequest, author);
        author = authorRepository.save(author);
        
        log.info("Successfully updated author with id: {}", id);
        return enrichAuthorDetailDTO(authorMapper.toDetailDTO(author));
    }
    
    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        log.info("Deleting author with id: {}", id);
        
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Author not found with id: " + id));
        
        // Check if author has books
        Long bookCount = authorRepository.countBooksByAuthorId(id);
        if (bookCount > 0) {
            throw new IllegalStateException("Cannot delete author: " + bookCount + " books are associated with this author");
        }
        
        authorRepository.delete(author);
        log.info("Successfully deleted author with id: {}", id);
    }
    
    @Override
    public Page<AuthorDTO> getAllAuthors(Pageable pageable) {
        log.debug("Fetching all authors with pagination");
        Page<Author> authorPage = authorRepository.findAll(pageable);
        return authorPage.map(this::enrichAuthorDTO);
    }
    
    @Override
    public Page<AuthorDTO> searchAuthors(String keyword, Pageable pageable) {
        log.debug("Searching authors with keyword: {}", keyword);
        Page<Author> authorPage = authorRepository.searchByKeyword(keyword, pageable);
        return authorPage.map(this::enrichAuthorDTO);
    }
    
    @Override
    public List<AuthorDTO> getAuthorsByNationality(String nationality) {
        log.debug("Fetching authors by nationality: {}", nationality);
        List<Author> authors = authorRepository.findByNationality(nationality);
        return authors.stream()
                .map(this::enrichAuthorDTO)
                .toList();
    }
    
    @Override
    public List<AuthorDTO> getMostProlificAuthors(int limit) {
        log.debug("Fetching most prolific authors with limit: {}", limit);
        Pageable pageable = PageRequest.of(0, limit);
        List<Author> authors = authorRepository.findMostProlificAuthors(pageable);
        return authors.stream()
                .map(this::enrichAuthorDTO)
                .toList();
    }
    
    @Override
    public List<AuthorDTO> searchAuthorsByName(String name) {
        log.debug("Searching authors by name: {}", name);
        List<Author> authors = authorRepository.findByNameContainingIgnoreCase(name);
        return authors.stream()
                .map(this::enrichAuthorDTO)
                .toList();
    }
    
    @Override
    public Long getBookCount(Long authorId) {
        return authorRepository.countBooksByAuthorId(authorId);
    }
    
    @Override
    public List<String> getAllNationalities() {
        log.debug("Fetching all nationalities");
        return authorRepository.findAllNationalities();
    }
    
    // Helper methods
    
    private AuthorDTO enrichAuthorDTO(Author author) {
        AuthorDTO dto = authorMapper.toDTO(author);
        dto.setBookCount(authorRepository.countBooksByAuthorId(author.getId()));
        dto.setAge(calculateAge(author.getBirthDate(), author.getDeathDate()));
        dto.setIsAlive(author.getDeathDate() == null);
        return dto;
    }
    
    private AuthorDetailDTO enrichAuthorDetailDTO(AuthorDetailDTO dto) {
        dto.setBookCount(authorRepository.countBooksByAuthorId(dto.getId()));
        dto.setAge(calculateAge(dto.getBirthDate(), dto.getDeathDate()));
        dto.setIsAlive(dto.getDeathDate() == null);
        return dto;
    }
    
    private Integer calculateAge(LocalDate birthDate, LocalDate deathDate) {
        if (birthDate == null) {
            return null;
        }
        
        LocalDate endDate = deathDate != null ? deathDate : LocalDate.now();
        return Period.between(birthDate, endDate).getYears();
    }
}