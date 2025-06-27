package org.library.fileservice.service;

import io.minio.StatObjectResponse;

import java.io.InputStream;
import java.util.List;

public interface MinioService {
    void uploadObject(String objectName, InputStream inputStream, long size, String contentType);

    InputStream getObject(String objectName);

    StatObjectResponse getObjectInfo(String objectName);

    List<String> listObjects(String prefix);

    void deleteObject(String objectName);
}
