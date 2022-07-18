package dev.soffa.foundation.storage;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import dev.soffa.foundation.commons.DateUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class S3Client implements ObjectStorageClient {

    private static final Logger LOG = Logger.get(S3Client.class);
    private final AmazonS3 client;
    private String defaultBucketName;

    public S3Client(String endpoint, String accessKey, String secretKey, String defaultBucketName) {
        this(endpoint, accessKey, secretKey);
        this.defaultBucketName = defaultBucketName;
    }

    public S3Client(ObjectStorageConfig config) {
        this(config.getEndpoint(), config.getAccessKey(), config.getSecretKey(), config.getBucket());
    }

    @SneakyThrows
    public S3Client(String endpoint, String accessKey, String secretKey) {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            //HttpProxyConfig proxyConfig = HttpProxyConfig.getProxy();
            ClientConfiguration config = new ClientConfiguration();
            config.setSignerOverride("AWSS3V4SignerType");
			/*if (proxyConfig != null) {
				config.setProtocol(Protocol.HTTP);
				config.setProxyHost(proxyConfig.getProxyHost());
				config.setProxyPort(proxyConfig.getProxyPort());
				config.setProxyDomain(proxyConfig.getProxyDomain());
				config.setProxyUsername(proxyConfig.getProxyUsername());
				config.setProxyPassword(proxyConfig.getProxyPassword());
			}*/
            LOG.info("S3 Endpoint is: %s", endpoint);
            client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(endpoint, Regions.US_EAST_1.name()))
                .withClientConfiguration(config).enablePathStyleAccess()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        } catch (Exception e) {
            throw new TechnicalException("S3_CLIENT_INIT_ERR", e);
        }
    }

    @Override
    public void upload(InputStream source, String objectName, String contentType) {
        upload(source, defaultBucketName, objectName, contentType);
    }

    @SneakyThrows
    @Override
    public void upload(InputStream source, String bucket, String objectName, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentEncoding(StandardCharsets.UTF_8.name());
            metadata.setContentType(contentType);
            client.putObject(bucket, objectName, source, metadata);
        } catch (Exception e) {
            throw new TechnicalException("S3_UPLOAD_ERROR", e);
        }
    }

    @Override
    public void upload(File source, String objectName) {
        upload(source, defaultBucketName, objectName);
    }

    @SneakyThrows
    @Override
    public void upload(File source, String bucket, String objectName) {
        try {
            client.putObject(bucket, objectName, source);
        } catch (Exception e) {
            throw new TechnicalException("S3_UPLOAD_ERROR", e);
        }
    }

    @SneakyThrows
    @Override
    public String downloadBase64(String bucket, String objectName) {
        return Base64.getEncoder().encodeToString(IOUtils.toByteArray(client.getObject(bucket, objectName).getObjectContent()));
    }

    @Override
    public String getDownloadUrl(String objectName, long expiresInMinutes) {
        return getDownloadUrl(defaultBucketName, objectName, expiresInMinutes);
    }

    @SneakyThrows
    @Override
    public String getDownloadUrl(String bucket, String objectName, long expiresInMinutes) {
        try {
            return client.generatePresignedUrl(bucket, objectName, DateUtil.plusHours(new Date(), 2)).toURI()
                .toString();
        } catch (Exception e) {
            throw new TechnicalException("S3_DOWNLOAD_URL_ERROR", e);
        }
    }

    @Override
    public String getUploadUrl(String objectName, long expiresInMinutes) {
        return getUploadUrl(defaultBucketName, objectName, expiresInMinutes);
    }

    @SneakyThrows
    @Override
    public String getUploadUrl(String bucket, String objectName, long expiresInMinutes) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, objectName)
            .withMethod(HttpMethod.PUT).withExpiration(DateUtil.plusHours(new Date(), 2));
        return client.generatePresignedUrl(generatePresignedUrlRequest).toURI().toString();
    }
}
