package com.library.service;

import com.library.dto.document.*;
import com.library.entity.Book;
import com.library.entity.Document;
import com.library.entity.enums.AccessLevel;
import com.library.entity.enums.AccessType;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.DocumentMapper;
import com.library.repository.BookRepository;
import com.library.repository.DocumentAccessLogRepository;
import com.library.repository.DocumentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentAccessLogRepository accessLogRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private DocumentAccessControlService accessControlService;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DocumentService documentService;
    private Document testDocument;
    private DocumentDTO testDocumentDTO;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Setup test data
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");

        testDocument = Document.builder()
                .id(1L)
                .title("Test Document")
                .description("Test Description")
                .originalFileName("test.pdf")
                .fileName("123456_test.pdf")
                .fileType("pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .objectKey("temp_123456_test.pdf")
                .accessLevel(AccessLevel.PUBLIC)
                .book(testBook)
                .uploadedBy("user123")
                .downloadCount(0)
                .isActive(true)
                .build();

        testDocumentDTO = DocumentDTO.builder()
                .id(1L)
                .title("Test Document")
                .description("Test Description")
                .originalFileName("test.pdf")
                .fileType("pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .accessLevel(AccessLevel.PUBLIC)
                .bookId(1L)
                .bookTitle("Test Book")
                .uploadedBy("user123")
                .downloadCount(0)
                .isActive(true)
                .canAccess(true)
                .downloadUrl("/api/v1/documents/1/download")
                .viewUrl("/api/v1/documents/1/view")
                .uploaderName("User user123")
                .build();

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.isAuthenticated()).thenReturn(true);
    }
    @Test
    void testUploadDocument_Success() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );

        CreateDocumentRequestDTO requestDTO = CreateDocumentRequestDTO.builder()
                .title("Test Document")
                .description("Test Description")
                .accessLevel(AccessLevel.PUBLIC)
                .bookId(1L)
                .build();

        when(accessControlService.canUploadDocuments()).thenReturn(true);
        when(documentMapper.toEntity(any(CreateDocumentRequestDTO.class))).thenReturn(testDocument);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        when(documentMapper.toDTO(any(Document.class))).thenReturn(testDocumentDTO);
        when(accessControlService.canAccessDocument(any(Document.class))).thenReturn(true);

        // Act
        DocumentDTO result = documentService.uploadDocument(file, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        assertEquals(AccessLevel.PUBLIC, result.getAccessLevel());
        assertEquals(true, result.getCanAccess());
        verify(documentRepository).save(any(Document.class));
        verify(bookRepository).findById(1L);
    }

    @Test
    void testUploadDocument_NoPermission() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );
        CreateDocumentRequestDTO requestDTO = new CreateDocumentRequestDTO();

        when(accessControlService.canUploadDocuments()).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
            documentService.uploadDocument(file, requestDTO)
        );
    }

    @Test
    void testGetDocument_Success() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(testDocument)).thenReturn(true);
        when(documentMapper.toDTO(testDocument)).thenReturn(testDocumentDTO);

        // Act
        DocumentDTO result = documentService.getDocument(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Document", result.getTitle());
        assertEquals(true, result.getCanAccess());
        verify(accessLogRepository).save(any()); // Verify access log
    }

    @Test
    void testGetDocument_NotFound() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            documentService.getDocument(1L)
        );
    }

    @Test
    void testGetDocument_AccessDenied() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(testDocument)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
            documentService.getDocument(1L)
        );

        // Verify access denied log
        verify(accessLogRepository).save(argThat(log ->
            log.getAccessType() == AccessType.ACCESS_DENIED
        ));
    }
    @Test
    void testSearchDocuments() {
        // Arrange
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .searchTerm("test")
                .fileTypes(Arrays.asList("pdf"))
                .isActive(true)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Document> documentPage = new PageImpl<>(Arrays.asList(testDocument));

        when(documentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(documentPage);
        when(documentMapper.toDTO(testDocument)).thenReturn(testDocumentDTO);
        when(accessControlService.canAccessDocument(testDocument)).thenReturn(true);

        // Act
        Page<DocumentDTO> result = documentService.searchDocuments(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Document", result.getContent().get(0).getTitle());
        assertEquals(true, result.getContent().get(0).getCanAccess());
    }
    @Test
    void testGetDownloadUrl_TemporarilyDisabled() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(testDocument)).thenReturn(true);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () ->
            documentService.getDownloadUrl(1L)
        );

        // Verify that download count was still incremented and access was logged
        verify(documentRepository).incrementDownloadCount(1L);
        verify(accessLogRepository).save(argThat(log ->
            log.getAccessType() == AccessType.DOWNLOAD
        ));
    }

    @Test
    void testUpdateDocument_Success() {
        // Arrange
        UpdateDocumentRequestDTO updateDTO = UpdateDocumentRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .accessLevel(AccessLevel.LOGGED_IN_USER)
                .build();

        Document updatedDocument = Document.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .accessLevel(AccessLevel.LOGGED_IN_USER)
                .build();

        DocumentDTO updatedDocumentDTO = DocumentDTO.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .accessLevel(AccessLevel.LOGGED_IN_USER)
                .canAccess(true)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(accessControlService.canManageDocument(testDocument)).thenReturn(true);
        when(documentRepository.save(any(Document.class))).thenReturn(updatedDocument);
        when(documentMapper.toDTO(any(Document.class))).thenReturn(updatedDocumentDTO);
        when(accessControlService.canAccessDocument(any(Document.class))).thenReturn(true);

        // Act
        DocumentDTO result = documentService.updateDocument(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(AccessLevel.LOGGED_IN_USER, result.getAccessLevel());
        assertEquals(true, result.getCanAccess());
        verify(documentMapper).updateEntity(updateDTO, testDocument);
        verify(documentRepository).save(testDocument);
    }

    @Test
    void testDeleteDocument_Success() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(accessControlService.canManageDocument(testDocument)).thenReturn(true);

        // Act
        documentService.deleteDocument(1L);

        // Assert
        verify(documentRepository).softDeleteDocument(1L);
    }
    @Test
    void testGetStatistics() {
        // Arrange
        when(accessControlService.canViewStatistics()).thenReturn(true);
        when(documentRepository.countActiveDocuments()).thenReturn(1L);
        when(documentRepository.sumDownloadCounts()).thenReturn(0L);
        when(documentRepository.sumFileSizes()).thenReturn(1024L);
        when(documentRepository.countDocumentsByAccessLevel()).thenReturn(Arrays.asList(
                new Object[]{"PUBLIC", 1L}
        ));
        when(documentRepository.countDocumentsByFileType()).thenReturn(Arrays.asList(
                new Object[]{"pdf", 1L}
        ));
        when(documentRepository.findDocumentsInDateRange(any(), any(), any())).thenReturn(
                new PageImpl<>(Arrays.asList(testDocument))
        );

        // Act
        DocumentStatisticsDTO stats = documentService.getStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(1L, stats.getTotalDocuments());
        assertEquals(0L, stats.getTotalDownloads());
        assertEquals(1024L, stats.getTotalSize());
        assertEquals("1.0 KB", stats.getTotalSizeFormatted());
        assertNotNull(stats.getDocumentsByAccessLevel());
        assertNotNull(stats.getDocumentsByFileType());
        assertEquals(1L, stats.getDocumentsByAccessLevel().get("PUBLIC"));
        assertEquals(1L, stats.getDocumentsByFileType().get("pdf"));
    }
    @Test
    void testGetMostDownloadedDocuments() {
        // Arrange
        testDocument.setDownloadCount(10);
        when(documentRepository.findMostDownloadedDocuments(any(Pageable.class)))
                .thenReturn(Arrays.asList(testDocument));
        when(documentMapper.toDTO(testDocument)).thenReturn(testDocumentDTO);
        when(accessControlService.canAccessDocument(testDocument)).thenReturn(true);

        // Act
        List<DocumentDTO> result = documentService.getMostDownloadedDocuments(5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Document", result.get(0).getTitle());
        assertEquals(true, result.get(0).getCanAccess());
    }
}