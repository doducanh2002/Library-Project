package org.library.fileservice.service;

import org.library.fileservice.model.FileMetadata;
import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileService {
    FileMetadata uploadFile(MultipartFile file);

    FileMetadata getFileMetadata(String fileId);

    InputStream getFileStream(String fileId);

    List<FileMetadata> getAllFiles();

    StatObjectResponse getFileInfo(String fileId);
    
    void deleteFile(String fileId);
}
