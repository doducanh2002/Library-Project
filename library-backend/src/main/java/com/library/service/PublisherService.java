package com.library.service;

import com.library.dto.CreatePublisherRequestDTO;
import com.library.dto.PublisherDTO;
import com.library.dto.PublisherDetailDTO;
import com.library.dto.UpdatePublisherRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PublisherService {
    
    // CRUD Operations
    PublisherDetailDTO createPublisher(CreatePublisherRequestDTO createRequest);
    
    PublisherDetailDTO getPublisherById(Long id);
    
    PublisherDetailDTO getPublisherWithBooks(Long id);
    
    PublisherDetailDTO updatePublisher(Long id, UpdatePublisherRequestDTO updateRequest);
    
    void deletePublisher(Long id);
    
    // Listing methods
    Page<PublisherDTO> getAllPublishers(Pageable pageable);
    
    List<PublisherDTO> searchPublishersByName(String name);
    
    List<PublisherDTO> getPublishersByEstablishedYear(Integer startYear, Integer endYear);
    
    List<PublisherDTO> getMostActivePublishers(int limit);
    
    // Validation methods
    boolean existsByName(String name);
    
    // Statistics
    Long getBookCount(Long publisherId);
}