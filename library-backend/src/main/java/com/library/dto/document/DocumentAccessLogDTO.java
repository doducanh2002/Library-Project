package com.library.dto.document;

import com.library.entity.enums.AccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAccessLogDTO {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private String userId;
    private String userName;
    private AccessType accessType;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime accessedAt;
}