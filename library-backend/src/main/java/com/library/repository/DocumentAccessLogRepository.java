package com.library.repository;

import com.library.entity.DocumentAccessLog;
import com.library.entity.enums.AccessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, Long> {
    
    // Find logs by document
    Page<DocumentAccessLog> findByDocumentId(Long documentId, Pageable pageable);
    
    // Find logs by user
    Page<DocumentAccessLog> findByUserId(String userId, Pageable pageable);
    
    // Find logs by access type
    List<DocumentAccessLog> findByAccessType(AccessType accessType);
    
    // Find logs in date range
    @Query("SELECT dal FROM DocumentAccessLog dal WHERE " +
           "dal.accessedAt BETWEEN :startDate AND :endDate")
    Page<DocumentAccessLog> findLogsInDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);
    
    // Count accesses by document
    @Query("SELECT COUNT(dal) FROM DocumentAccessLog dal WHERE dal.document.id = :documentId")
    Long countAccessesByDocument(@Param("documentId") Long documentId);
    
    // Count unique users who accessed a document
    @Query("SELECT COUNT(DISTINCT dal.userId) FROM DocumentAccessLog dal WHERE dal.document.id = :documentId")
    Long countUniqueUsersByDocument(@Param("documentId") Long documentId);
    
    // Find most accessed documents
    @Query("SELECT dal.document.id, COUNT(dal) as accessCount FROM DocumentAccessLog dal " +
           "GROUP BY dal.document.id ORDER BY accessCount DESC")
    List<Object[]> findMostAccessedDocuments(Pageable pageable);
    
    // Get access statistics by type
    @Query("SELECT dal.accessType, COUNT(dal) FROM DocumentAccessLog dal " +
           "WHERE dal.document.id = :documentId GROUP BY dal.accessType")
    List<Object[]> getAccessStatisticsByType(@Param("documentId") Long documentId);
    
    // Check if user has accessed document
    boolean existsByDocumentIdAndUserId(Long documentId, String userId);
    
    // Find recent accesses
    @Query("SELECT dal FROM DocumentAccessLog dal ORDER BY dal.accessedAt DESC")
    Page<DocumentAccessLog> findRecentAccesses(Pageable pageable);
    
    // Clean up old logs
    @Query("DELETE FROM DocumentAccessLog dal WHERE dal.accessedAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}