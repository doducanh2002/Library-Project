package com.library.specification;

import com.library.dto.document.DocumentSearchCriteria;
import com.library.entity.Document;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentSpecification {
    
    public static Specification<Document> withCriteria(DocumentSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Search term (title or description)
            if (criteria.getSearchTerm() != null && !criteria.getSearchTerm().isEmpty()) {
                String searchPattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchPattern
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }
            
            // File types
            if (criteria.getFileTypes() != null && !criteria.getFileTypes().isEmpty()) {
                predicates.add(root.get("fileType").in(criteria.getFileTypes()));
            }
            
            // Access level
            if (criteria.getAccessLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accessLevel"), criteria.getAccessLevel()));
            }
            
            // Book ID
            if (criteria.getBookId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("book").get("id"), criteria.getBookId()));
            }
            
            // Uploaded by
            if (criteria.getUploadedBy() != null && !criteria.getUploadedBy().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("uploadedBy"), criteria.getUploadedBy()));
            }
            
            // Date range
            if (criteria.getUploadedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), criteria.getUploadedAfter()
                ));
            }
            
            if (criteria.getUploadedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), criteria.getUploadedBefore()
                ));
            }
            
            // Minimum download count
            if (criteria.getMinDownloadCount() != null && criteria.getMinDownloadCount() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("downloadCount"), criteria.getMinDownloadCount()
                ));
            }
            
            // Active status
            if (criteria.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), criteria.getIsActive()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<Document> isActive() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("isActive"), true);
    }
    
    public static Specification<Document> byBookId(Long bookId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("book").get("id"), bookId);
    }
    
    public static Specification<Document> byUploader(String uploaderId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("uploadedBy"), uploaderId);
    }
    
    public static Specification<Document> publicDocuments() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("accessLevel"), "PUBLIC");
    }
}