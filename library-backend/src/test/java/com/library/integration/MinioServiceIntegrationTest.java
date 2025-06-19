package com.library.integration;

import com.library.client.MinioServiceClient;
import com.library.config.MinioConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for MinIO Service client
 */
@SpringBootTest
@TestPropertySource(properties = {
    "minio.service.url=http://localhost:8083"
})
class MinioServiceIntegrationTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private MinioConfig minioConfig;

    private MinioServiceClient minioServiceClient;

    @BeforeEach
    void setUp() {
        minioServiceClient = new MinioServiceClient(restTemplate, minioConfig);
    }

    @Test
    void testUploadFile_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test-document.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("objectKey", "documents/123456789_abcd1234.pdf");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        String objectKey = minioServiceClient.uploadFile(file, "documents");

        // Then
        assertNotNull(objectKey);
        assertEquals("documents/123456789_abcd1234.pdf", objectKey);
        verify(restTemplate).postForEntity(anyString(), any(), eq(Map.class));
    }

    @Test
    void testGenerateDownloadUrl_Success() {
        // Given
        String objectKey = "documents/test-file.pdf";
        int expiryMinutes = 60;

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("url", "/api/documents/download/" + objectKey);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        String downloadUrl = minioServiceClient.generateDownloadUrl(objectKey, expiryMinutes);

        // Then
        assertNotNull(downloadUrl);
        assertEquals("/api/documents/download/" + objectKey, downloadUrl);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void testGenerateViewUrl_Success() {
        // Given
        String objectKey = "documents/test-file.pdf";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("url", "/api/documents/view/" + objectKey);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        String viewUrl = minioServiceClient.generateViewUrl(objectKey);

        // Then
        assertNotNull(viewUrl);
        assertEquals("/api/documents/view/" + objectKey, viewUrl);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void testFileExists_True() {
        // Given
        String objectKey = "documents/existing-file.pdf";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("exists", true);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        boolean exists = minioServiceClient.fileExists(objectKey);

        // Then
        assertTrue(exists);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void testFileExists_False() {
        // Given
        String objectKey = "documents/non-existing-file.pdf";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("exists", false);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        boolean exists = minioServiceClient.fileExists(objectKey);

        // Then
        assertFalse(exists);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void testGetFileSize_Success() throws Exception {
        // Given
        String objectKey = "documents/test-file.pdf";
        long expectedSize = 1024L;

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("size", expectedSize);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
            .thenReturn(responseEntity);

        // When
        long fileSize = minioServiceClient.getFileSize(objectKey);

        // Then
        assertEquals(expectedSize, fileSize);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void testDeleteFile_Success() throws Exception {
        // Given
        String objectKey = "documents/test-file.pdf";

        // When
        assertDoesNotThrow(() -> minioServiceClient.deleteFile(objectKey));

        // Then
        verify(restTemplate).delete(anyString());
    }
}