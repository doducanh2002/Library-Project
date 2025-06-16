package com.library.repository;

import com.library.entity.Document;
import com.library.entity.enums.AccessLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    
    // Find by unique object key
    Optional<Document> findByObjectKey(String objectKey);
    
    // Find by book
    List<Document> findByBookIdAndIsActiveTrue(Long bookId);
    Page<Document> findByBookIdAndIsActiveTrue(Long bookId, Pageable pageable);
    
    // Find by uploader
    Page<Document> findByUploadedByAndIsActiveTrue(String uploadedBy, Pageable pageable);
    
    // Find by access level
    Page<Document> findByAccessLevelAndIsActiveTrue(AccessLevel accessLevel, Pageable pageable);
    
    // Find public documents
    @Query("SELECT d FROM Document d WHERE d.accessLevel = 'PUBLIC' AND d.isActive = true")
    Page<Document> findPublicDocuments(Pageable pageable);
    
    // Search documents by title or description
    @Query("SELECT d FROM Document d WHERE d.isActive = true AND " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Document> searchDocuments(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find documents by file type
    Page<Document> findByFileTypeAndIsActiveTrue(String fileType, Pageable pageable);
    
    // Find documents uploaded in date range
    @Query("SELECT d FROM Document d WHERE d.isActive = true AND " +
           "d.createdAt BETWEEN :startDate AND :endDate")
    Page<Document> findDocumentsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);
    
    // Update download count
    @Modifying
    @Query("UPDATE Document d SET d.downloadCount = d.downloadCount + 1 WHERE d.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
    
    // Find most downloaded documents
    @Query("SELECT d FROM Document d WHERE d.isActive = true ORDER BY d.downloadCount DESC")
    List<Document> findMostDownloadedDocuments(Pageable pageable);
    
    // Check if document exists for a book
    boolean existsByBookIdAndIsActiveTrue(Long bookId);
    
    // Count documents by access level
    @Query("SELECT d.accessLevel, COUNT(d) FROM Document d WHERE d.isActive = true " +
           "GROUP BY d.accessLevel")
    List<Object[]> countDocumentsByAccessLevel();
    
    // Find documents by multiple file types
    @Query("SELECT d FROM Document d WHERE d.isActive = true AND d.fileType IN :fileTypes")
    Page<Document> findByFileTypes(@Param("fileTypes") List<String> fileTypes, Pageable pageable);
    
    // Find orphaned documents (not linked to any book)
    @Query("SELECT d FROM Document d WHERE d.book IS NULL AND d.isActive = true")
    Page<Document> findOrphanedDocuments(Pageable pageable);
    
    // Soft delete document
    @Modifying
    @Query("UPDATE Document d SET d.isActive = false WHERE d.id = :id")
    void softDeleteDocument(@Param("id") Long id);
    
    // Find documents accessible by user (considering access levels)
    @Query("SELECT d FROM Document d WHERE d.isActive = true AND " +
           "(d.accessLevel = 'PUBLIC' OR " +
           "(d.accessLevel = 'LOGGED_IN_USER' AND :userId IS NOT NULL) OR " +
           "(d.accessLevel = 'PRIVATE' AND d.uploadedBy = :userId))")
    Page<Document> findAccessibleDocuments(@Param("userId") String userId, Pageable pageable);
}