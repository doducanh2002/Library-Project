package com.library.dto.document;

import com.library.entity.enums.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSearchCriteria {
    private String searchTerm;
    private List<String> fileTypes;
    private AccessLevel accessLevel;
    private Long bookId;
    private String uploadedBy;
    private LocalDateTime uploadedAfter;
    private LocalDateTime uploadedBefore;
    private Integer minDownloadCount;
    private Boolean isActive = true;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}