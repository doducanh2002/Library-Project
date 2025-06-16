package com.library.dto.document;

import com.library.entity.enums.AccessLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDocumentRequestDTO {
    
    @NotBlank(message = "Document title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Access level is required")
    private AccessLevel accessLevel = AccessLevel.PUBLIC;
    
    @Min(value = 1, message = "Book ID must be positive")
    private Long bookId;
    
    // Metadata as JSON string
    private String metadata;
    
    // File will be uploaded separately via multipart
}