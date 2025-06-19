package com.library.service;

import com.library.config.MinioConfig;
import com.library.exception.FileStorageException;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {
    
    @Mock
    private MinioClient minioClient;
    
    @Mock
    private MinioConfig minioConfig;
    
    @InjectMocks
    private MinioService minioService;
    
    @BeforeEach
    void setUp() {
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioConfig.getRegion()).thenReturn("us-east-1");
    }
    
    @Test
    void testUploadFile_Success() throws Exception {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );
        String folder = "documents";
        
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        
        // Act
        String result = minioService.uploadFile(file, folder);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("documents/"));
        assertTrue(result.endsWith(".pdf"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
    
    @Test
    void testUploadFile_EmptyFile() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[0]
        );
        String folder = "documents";
        
        // Act & Assert
        assertThrows(FileStorageException.class, () -> 
            minioService.uploadFile(file, folder)
        );
    }
    
    @Test
    void testUploadFile_FileTooLarge() {
        // Arrange
        byte[] largeContent = new byte[101 * 1024 * 1024]; // 101MB
        MultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeContent
        );
        String folder = "documents";
        
        // Act & Assert
        assertThrows(FileStorageException.class, () -> 
            minioService.uploadFile(file, folder)
        );
    }
    
    @Test
    void testUploadFile_InvalidFileType() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file", "test.exe", "application/x-msdownload", "test content".getBytes()
        );
        String folder = "documents";
        
        // Act & Assert
        assertThrows(FileStorageException.class, () -> 
            minioService.uploadFile(file, folder)
        );
    }
    
    @Test
    void testDownloadFile_Success() throws Exception {
        // Arrange
        String objectKey = "documents/test.pdf";
        InputStream mockStream = new ByteArrayInputStream("test content".getBytes());
        
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(
            new GetObjectResponse(null, "", "", "", mockStream)
        );
        
        // Act
        InputStream result = minioService.downloadFile(objectKey);
        
        // Assert
        assertNotNull(result);
        assertEquals("test content", new String(result.readAllBytes()));
    }
    
    @Test
    void testDownloadFile_NotFound() throws Exception {
        // Arrange
        String objectKey = "documents/nonexistent.pdf";
        
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new MinioException("Object not found"));
        
        // Act & Assert
        assertThrows(FileStorageException.class, () -> 
            minioService.downloadFile(objectKey)
        );
    }
    
    @Test
    void testGenerateDownloadUrl_Success() throws Exception {
        // Arrange
        String objectKey = "documents/test.pdf";
        String expectedUrl = "https://minio.example.com/test-bucket/documents/test.pdf?X-Amz-Signature=...";
        
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);
        
        // Act
        String result = minioService.generateDownloadUrl(objectKey, 60);
        
        // Assert
        assertEquals(expectedUrl, result);
        verify(minioClient).getPresignedObjectUrl(argThat(args -> 
            args.method() == Method.GET && args.expiry() == 60
        ));
    }
    
    @Test
    void testGenerateViewUrl_Success() throws Exception {
        // Arrange
        String objectKey = "documents/test.pdf";
        String expectedUrl = "https://minio.example.com/test-bucket/documents/test.pdf?response-content-type=application/pdf";
        
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);
        
        // Act
        String result = minioService.generateViewUrl(objectKey);
        
        // Assert
        assertEquals(expectedUrl, result);
    }
    
    @Test
    void testDeleteFile_Success() throws Exception {
        // Arrange
        String objectKey = "documents/test.pdf";
        
        // Act
        minioService.deleteFile(objectKey);
        
        // Assert
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }
    
    @Test
    void testFileExists_True() throws Exception {
        // Arrange
        String objectKey = "documents/test.pdf";
        StatObjectResponse mockResponse = mock(StatObjectResponse.class);
        
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockResponse);
        
        // Act
        boolean result = minioService.fileExists(objectKey);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testFileExists_False() throws Exception {
        // Arrange
        String objectKey = "documents/nonexistent.pdf";
        
        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenThrow(new MinioException("Object not found"));
        
        // Act
        boolean result = minioService.fileExists(objectKey);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testGetFileMetadata_Success() throws Exception {
        // Arrange
        String objectKey = "documents/test.pdf";
        StatObjectResponse mockResponse = mock(StatObjectResponse.class);
        
        when(mockResponse.size()).thenReturn(1024L);
        when(mockResponse.contentType()).thenReturn("application/pdf");
        when(mockResponse.etag()).thenReturn("abc123");
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockResponse);
        
        // Act
        var metadata = minioService.getFileMetadata(objectKey);
        
        // Assert
        assertNotNull(metadata);
        assertEquals("1024", metadata.get("size"));
        assertEquals("application/pdf", metadata.get("contentType"));
        assertEquals("abc123", metadata.get("etag"));
    }
}