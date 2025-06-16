package com.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.library.entity.enums.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Document title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank(message = "Original file name is required")
    @Size(max = 255, message = "Original file name must not exceed 255 characters")
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotBlank(message = "File type is required")
    @Size(max = 100, message = "File type must not exceed 100 characters")
    @Column(name = "file_type", nullable = false)
    private String fileType;

    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "MIME type is required")
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @NotBlank(message = "Bucket name is required")
    @Size(max = 100, message = "Bucket name must not exceed 100 characters")
    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @NotBlank(message = "Object key is required")
    @Size(max = 500, message = "Object key must not exceed 500 characters")
    @Column(name = "object_key", nullable = false, unique = true)
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_documents_book_id"))
    @JsonIgnore
    private Book book;

    @NotBlank(message = "Uploaded by is required")
    @Size(max = 36, message = "Uploaded by must not exceed 36 characters")
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DocumentAccessLog> accessLogs;

    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    public String getFileExtension() {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", accessLevel=" + accessLevel +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}