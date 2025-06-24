package com.library.dto.document;

import com.library.entity.enums.AccessLevel;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {
    private Long id;
    private String title;
    private String description;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private AccessLevel accessLevel;
    private Long bookId;
    private String bookTitle;
    private String uploadedBy;
    private String uploaderName;
    private Integer downloadCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Boolean canAccess;
    private String downloadUrl;
    private String viewUrl;
}