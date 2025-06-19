package com.library.service;

import com.library.entity.Book;
import com.library.entity.Document;
import com.library.entity.enums.AccessLevel;
import com.library.repository.LoanRepository;
import com.library.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentAccessControlServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private DocumentAccessControlService accessControlService;
    
    private Document publicDocument;
    private Document loggedInUserDocument;
    private Document privateDocument;
    private Document restrictedDocument;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        
        // Setup test documents
        publicDocument = Document.builder()
                .id(1L)
                .title("Public Document")
                .accessLevel(AccessLevel.PUBLIC)
                .isActive(true)
                .uploadedBy("user123")
                .build();
        
        loggedInUserDocument = Document.builder()
                .id(2L)
                .title("Logged In User Document")
                .accessLevel(AccessLevel.LOGGED_IN_USER)
                .isActive(true)
                .uploadedBy("user123")
                .build();
        
        privateDocument = Document.builder()
                .id(3L)
                .title("Private Document")
                .accessLevel(AccessLevel.PRIVATE)
                .isActive(true)
                .uploadedBy("user123")
                .build();
        
        restrictedDocument = Document.builder()
                .id(4L)
                .title("Restricted Document")
                .accessLevel(AccessLevel.RESTRICTED_BY_BOOK_OWNERSHIP)
                .isActive(true)
                .uploadedBy("user456")
                .book(testBook)
                .build();
    }
    
    @Test
    void testCanAccessDocument_PublicDocument() {
        // Test anonymous user
        when(authentication.isAuthenticated()).thenReturn(false);
        assertTrue(accessControlService.canAccessDocument(publicDocument));
        
        // Test authenticated user
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        assertTrue(accessControlService.canAccessDocument(publicDocument));
    }
    
    @Test
    void testCanAccessDocument_NullDocument() {
        assertFalse(accessControlService.canAccessDocument(null));
    }
    
    @Test
    void testCanAccessDocument_InactiveDocument() {
        publicDocument.setIsActive(false);
        assertFalse(accessControlService.canAccessDocument(publicDocument));
    }
    
    @Test
    void testCanAccessDocument_LoggedInUserDocument_Authenticated() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.getPrincipal()).thenReturn("user123");
        
        assertTrue(accessControlService.canAccessDocument(loggedInUserDocument));
    }
    
    @Test
    void testCanAccessDocument_LoggedInUserDocument_NotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        
        assertFalse(accessControlService.canAccessDocument(loggedInUserDocument));
    }
    
    @Test
    void testCanAccessDocument_PrivateDocument_AsUploader() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.getPrincipal()).thenReturn("user123");
        
        assertTrue(accessControlService.canAccessDocument(privateDocument));
    }
    
    @Test
    void testCanAccessDocument_PrivateDocument_NotUploader() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("otheruser");
        when(authentication.getPrincipal()).thenReturn("otheruser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        assertFalse(accessControlService.canAccessDocument(privateDocument));
    }
    
    @Test
    void testCanAccessDocument_PrivateDocument_AsAdmin() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getPrincipal()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        
        assertTrue(accessControlService.canAccessDocument(privateDocument));
    }
    
    @Test
    void testCanAccessDocument_RestrictedDocument_WithLoan() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        when(loanRepository.existsByUserIdAndBookIdAndStatusIn(
            "user123", 1L, List.of("BORROWED", "APPROVED")
        )).thenReturn(true);
        
        assertTrue(accessControlService.canAccessDocument(restrictedDocument));
    }
    
    @Test
    void testCanAccessDocument_RestrictedDocument_WithPurchase() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        when(loanRepository.existsByUserIdAndBookIdAndStatusIn(
            "user123", 1L, List.of("BORROWED", "APPROVED")
        )).thenReturn(false);
        
        when(orderItemRepository.existsByBookIdAndOrderUserIdAndOrderStatusIn(
            1L, "user123", List.of("DELIVERED", "SHIPPED", "PROCESSING", "PAID")
        )).thenReturn(true);
        
        assertTrue(accessControlService.canAccessDocument(restrictedDocument));
    }
    
    @Test
    void testCanAccessDocument_RestrictedDocument_NoAccess() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        when(loanRepository.existsByUserIdAndBookIdAndStatusIn(
            anyString(), anyLong(), anyList()
        )).thenReturn(false);
        
        when(orderItemRepository.existsByBookIdAndOrderUserIdAndOrderStatusIn(
            anyLong(), anyString(), anyList()
        )).thenReturn(false);
        
        assertFalse(accessControlService.canAccessDocument(restrictedDocument));
    }
    
    @Test
    void testCanUploadDocuments_AsAdmin() {
        setupAuthorities("ROLE_ADMIN");
        assertTrue(accessControlService.canUploadDocuments());
    }
    
    @Test
    void testCanUploadDocuments_AsLibrarian() {
        setupAuthorities("ROLE_LIBRARIAN");
        assertTrue(accessControlService.canUploadDocuments());
    }
    
    @Test
    void testCanUploadDocuments_AsUser() {
        setupAuthorities("ROLE_USER");
        assertFalse(accessControlService.canUploadDocuments());
    }
    
    @Test
    void testCanManageDocument_AsOwner() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        assertTrue(accessControlService.canManageDocument(privateDocument));
    }
    
    @Test
    void testCanManageDocument_NotOwner() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("otheruser");
        when(authentication.getPrincipal()).thenReturn("otheruser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        assertFalse(accessControlService.canManageDocument(privateDocument));
    }
    
    @Test
    void testCanViewStatistics_AsAdmin() {
        setupAuthorities("ROLE_ADMIN");
        assertTrue(accessControlService.canViewStatistics());
    }
    
    @Test
    void testCanViewStatistics_AsUser() {
        setupAuthorities("ROLE_USER");
        assertFalse(accessControlService.canViewStatistics());
    }
    
    @Test
    void testGetDefaultAccessLevel_AsAdmin() {
        setupAuthorities("ROLE_ADMIN");
        assertEquals(AccessLevel.PUBLIC, accessControlService.getDefaultAccessLevel());
    }
    
    @Test
    void testGetDefaultAccessLevel_AsUser() {
        setupAuthorities("ROLE_USER");
        assertEquals(AccessLevel.LOGGED_IN_USER, accessControlService.getDefaultAccessLevel());
    }
    
    @Test
    void testGetCurrentUserRole() {
        setupAuthorities("ROLE_USER");
        assertEquals("ROLE_USER", accessControlService.getCurrentUserRole());
    }
    
    private void setupAuthorities(String... roles) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn("testuser");
        
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());
        
        when(authentication.getAuthorities()).thenReturn(authorities);
    }
}