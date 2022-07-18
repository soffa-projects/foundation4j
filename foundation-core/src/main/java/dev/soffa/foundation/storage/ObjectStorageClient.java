package dev.soffa.foundation.storage;

import java.io.File;
import java.io.InputStream;

public interface ObjectStorageClient {

    void upload(InputStream source, String bucket, String objectName, String contentType);

    void upload(File source, String bucket, String objectName);

    void upload(File source, String objectName);

    void upload(InputStream source, String objectName, String contentType);

    String downloadBase64(String bucket, String objectName);

    String getDownloadUrl(String bucket, String objectName, long expiresInMinutes);

    String getDownloadUrl(String objectName, long expiresInMinutes);

    String getUploadUrl(String bucket, String objectName, long expiresInMinutes);

    String getUploadUrl(String objectName, long expiresInMinutes);

}
