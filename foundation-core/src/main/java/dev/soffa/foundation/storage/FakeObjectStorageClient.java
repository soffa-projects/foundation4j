package dev.soffa.foundation.storage;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.storage.ObjectStorageClient;

import java.io.File;
import java.io.InputStream;

public class FakeObjectStorageClient implements ObjectStorageClient {

    private static final Logger LOG = Logger.get(FakeObjectStorageClient.class);

    @Override
    public void upload(InputStream source, String bucket, String objectName, String contentType) {
        LOG.info("FakeS3: upload to %s/%s", bucket, objectName);
    }

    @Override
    public void upload(File source, String bucket, String objectName) {
        LOG.info("FakeS3: upload file to to %s/%s", bucket, objectName);
    }

    @Override
    public void upload(File source, String objectName) {
        LOG.info("FakeS3: upload file to to %s", objectName);
    }

    @Override
    public void upload(InputStream source, String objectName, String contentType) {
        LOG.info("FakeS3: upload stream to to %s", objectName);
    }

    @Override
    public String getDownloadUrl(String bucket, String objectName, long expiresInMinutes) {
        return "mocked://" + bucket + "/" + objectName;
    }

    @Override
    public String getDownloadUrl(String objectName, long expiresInMinutes) {
        return "mocked://default/" + objectName;
    }

    @Override
    public String getUploadUrl(String bucket, String objectName, long expiresInMinutes) {
        return "mocked://" + bucket + "/" + objectName;
    }

    @Override
    public String getUploadUrl(String objectName, long expiresInMinutes) {
        return "mocked://default/" + objectName;
    }

    @Override
    public String downloadBase64(String bucket, String objectName) {
        return "";
    }
}
