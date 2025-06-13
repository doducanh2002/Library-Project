package com.library.service;

import com.library.dto.AuthorDTO;
import com.library.dto.AuthorDetailDTO;
import com.library.dto.CreateAuthorRequestDTO;
import com.library.dto.UpdateAuthorRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuthorService {
    
    // CRUD Operations
    AuthorDetailDTO createAuthor(CreateAuthorRequestDTO createRequest);
    
    AuthorDetailDTO getAuthorById(Long id);
    
    AuthorDetailDTO getAuthorWithBooks(Long id);
    
    AuthorDetailDTO updateAuthor(Long id, UpdateAuthorRequestDTO updateRequest);
    
    void deleteAuthor(Long id);
    
    // Listing methods
    Page<AuthorDTO> getAllAuthors(Pageable pageable);
    
    Page<AuthorDTO> searchAuthors(String keyword, Pageable pageable);
    
    List<AuthorDTO> getAuthorsByNationality(String nationality);
    
    List<AuthorDTO> getMostProlificAuthors(int limit);
    
    // Search methods
    List<AuthorDTO> searchAuthorsByName(String name);
    
    // Statistics
    Long getBookCount(Long authorId);
    
    List<String> getAllNationalities();
}