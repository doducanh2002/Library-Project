package com.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.library.entity.enums.AccessType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_document_access_logs_document_id"))
    @NotNull(message = "Document is required")
    @JsonIgnore
    private Document document;

    @NotBlank(message = "User ID is required")
    @Size(max = 36, message = "User ID must not exceed 36 characters")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    @NotNull(message = "Access type is required")
    private AccessType accessType;

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "accessed_at", nullable = false, updatable = false)
    private LocalDateTime accessedAt;

    @Override
    public String toString() {
        return "DocumentAccessLog{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", accessType=" + accessType +
                ", ipAddress='" + ipAddress + '\'' +
                ", accessedAt=" + accessedAt +
                '}';
    }
}