package com.library.mapper;

import com.library.dto.document.*;
import com.library.entity.Document;
import com.library.entity.DocumentAccessLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentMapper {
    
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "fileSizeFormatted", expression = "java(document.getFileSizeFormatted())")
    @Mapping(target = "canAccess", ignore = true)
    @Mapping(target = "downloadUrl", ignore = true)
    @Mapping(target = "viewUrl", ignore = true)
    @Mapping(target = "uploaderName", ignore = true)
    DocumentDTO toDTO(Document document);
    
    List<DocumentDTO> toDTOs(List<Document> documents);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "originalFileName", ignore = true)
    @Mapping(target = "fileType", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "bucketName", ignore = true)
    @Mapping(target = "objectKey", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "downloadCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    Document toEntity(CreateDocumentRequestDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "originalFileName", ignore = true)
    @Mapping(target = "fileType", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "bucketName", ignore = true)
    @Mapping(target = "objectKey", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "downloadCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    void updateEntity(UpdateDocumentRequestDTO dto, @MappingTarget Document document);
    
    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "documentTitle", source = "document.title")
    @Mapping(target = "userName", ignore = true)
    DocumentAccessLogDTO toAccessLogDTO(DocumentAccessLog accessLog);
    
    List<DocumentAccessLogDTO> toAccessLogDTOs(List<DocumentAccessLog> accessLogs);
}