package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.dto.CreatePublisherRequestDTO;
import com.library.dto.PublisherDTO;
import com.library.dto.PublisherDetailDTO;
import com.library.dto.UpdatePublisherRequestDTO;
import com.library.entity.Publisher;
import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.mapper.BookMapper;
import com.library.mapper.PublisherMapper;
import com.library.repository.PublisherRepository;
import com.library.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PublisherServiceImpl implements PublisherService {
    
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    private final BookMapper bookMapper;
    
    @Override
    @Transactional
    public PublisherDetailDTO createPublisher(CreatePublisherRequestDTO createRequest) {
        log.info("Creating new publisher with name: {}", createRequest.getName());
        
        // Validate unique name
        if (publisherRepository.existsByName(createRequest.getName())) {
            throw new DuplicateBookException("Publisher with name '" + createRequest.getName() + "' already exists");
        }
        
        // Validate established year if provided
        if (createRequest.getEstablishedYear() != null) {
            int currentYear = Year.now().getValue();
            if (createRequest.getEstablishedYear() > currentYear) {
                throw new IllegalArgumentException("Established year cannot be in the future");
            }
        }
        
        Publisher publisher = publisherMapper.toEntity(createRequest);
        publisher = publisherRepository.save(publisher);
        
        log.info("Successfully created publisher with id: {}", publisher.getId());
        return enrichPublisherDetailDTO(publisherMapper.toDetailDTO(publisher));
    }
    
    @Override
    public PublisherDetailDTO getPublisherById(Long id) {
        log.debug("Fetching publisher by id: {}", id);
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Publisher not found with id: " + id));
        
        return enrichPublisherDetailDTO(publisherMapper.toDetailDTO(publisher));
    }
    
    @Override
    public PublisherDetailDTO getPublisherWithBooks(Long id) {
        log.debug("Fetching publisher with books by id: {}", id);
        Publisher publisher = publisherRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new BookNotFoundException("Publisher not found with id: " + id));
        
        PublisherDetailDTO dto = enrichPublisherDetailDTO(publisherMapper.toDetailDTO(publisher));
        
        // Map books
        List<BookDTO> books = publisher.getBooks().stream()
                .map(bookMapper::toDTO)
                .toList();
        dto.setBooks(books);
        
        return dto;
    }
    
    @Override
    @Transactional
    public PublisherDetailDTO updatePublisher(Long id, UpdatePublisherRequestDTO updateRequest) {
        log.info("Updating publisher with id: {}", id);
        
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Publisher not found with id: " + id));
        
        // Validate unique name if changed
        if (updateRequest.getName() != null && 
            !updateRequest.getName().equals(publisher.getName()) &&
            publisherRepository.existsByName(updateRequest.getName())) {
            throw new DuplicateBookException("Publisher with name '" + updateRequest.getName() + "' already exists");
        }
        
        // Validate established year if provided
        if (updateRequest.getEstablishedYear() != null) {
            int currentYear = Year.now().getValue();
            if (updateRequest.getEstablishedYear() > currentYear) {
                throw new IllegalArgumentException("Established year cannot be in the future");
            }
        }
        
        publisherMapper.updateEntityFromDTO(updateRequest, publisher);
        publisher = publisherRepository.save(publisher);
        
        log.info("Successfully updated publisher with id: {}", id);
        return enrichPublisherDetailDTO(publisherMapper.toDetailDTO(publisher));
    }
    
    @Override
    @Transactional
    public void deletePublisher(Long id) {
        log.info("Deleting publisher with id: {}", id);
        
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Publisher not found with id: " + id));
        
        // Check if publisher has books
        Long bookCount = publisherRepository.countBooksByPublisherId(id);
        if (bookCount > 0) {
            throw new IllegalStateException("Cannot delete publisher: " + bookCount + " books are associated with this publisher");
        }
        
        publisherRepository.delete(publisher);
        log.info("Successfully deleted publisher with id: {}", id);
    }
    
    @Override
    public Page<PublisherDTO> getAllPublishers(Pageable pageable) {
        log.debug("Fetching all publishers with pagination");
        Page<Publisher> publisherPage = publisherRepository.findAll(pageable);
        return publisherPage.map(this::enrichPublisherDTO);
    }
    
    @Override
    public List<PublisherDTO> searchPublishersByName(String name) {
        log.debug("Searching publishers by name: {}", name);
        List<Publisher> publishers = publisherRepository.findByNameContainingIgnoreCase(name);
        return publishers.stream()
                .map(this::enrichPublisherDTO)
                .toList();
    }
    
    @Override
    public List<PublisherDTO> getPublishersByEstablishedYear(Integer startYear, Integer endYear) {
        log.debug("Fetching publishers established between {} and {}", startYear, endYear);
        List<Publisher> publishers = publisherRepository.findByEstablishedYearBetween(startYear, endYear);
        return publishers.stream()
                .map(this::enrichPublisherDTO)
                .toList();
    }
    
    @Override
    public List<PublisherDTO> getMostActivePublishers(int limit) {
        log.debug("Fetching most active publishers with limit: {}", limit);
        Pageable pageable = PageRequest.of(0, limit);
        List<Publisher> publishers = publisherRepository.findMostActivePublishers(pageable);
        return publishers.stream()
                .map(this::enrichPublisherDTO)
                .toList();
    }
    
    @Override
    public boolean existsByName(String name) {
        return publisherRepository.existsByName(name);
    }
    
    @Override
    public Long getBookCount(Long publisherId) {
        return publisherRepository.countBooksByPublisherId(publisherId);
    }
    
    // Helper methods
    
    private PublisherDTO enrichPublisherDTO(Publisher publisher) {
        PublisherDTO dto = publisherMapper.toDTO(publisher);
        dto.setBookCount(publisherRepository.countBooksByPublisherId(publisher.getId()));
        dto.setYearsInBusiness(calculateYearsInBusiness(publisher.getEstablishedYear()));
        return dto;
    }
    
    private PublisherDetailDTO enrichPublisherDetailDTO(PublisherDetailDTO dto) {
        dto.setBookCount(publisherRepository.countBooksByPublisherId(dto.getId()));
        dto.setYearsInBusiness(calculateYearsInBusiness(dto.getEstablishedYear()));
        return dto;
    }
    
    private Integer calculateYearsInBusiness(Integer establishedYear) {
        if (establishedYear == null) {
            return null;
        }
        
        int currentYear = Year.now().getValue();
        return currentYear - establishedYear;
    }
}