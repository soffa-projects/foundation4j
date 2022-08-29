package ext.springboot;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.storage.FakeObjectStorageClient;
import dev.soffa.foundation.storage.ObjectStorageClient;
import dev.soffa.foundation.storage.ObjectStorageConfig;
import dev.soffa.foundation.storage.S3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectStorageClient createS3Client(@Value("${app.s3.url}") String url) {
        if (url.isEmpty()) {
            return new FakeObjectStorageClient();
        }
        UrlInfo uri = UrlInfo.parse(url);
        Logger.platform.info(
            "S3 Client configured: server=%s user=******%s bucket=%s",
            uri.getHostname(),
            TextUtil.takeLast(uri.getUsername(), 5),
            uri.getPath()
        );
        ObjectStorageConfig config = new ObjectStorageConfig(
            uri.getHostname(), uri.getUsername(), uri.getPassword(), uri.getPath().replaceAll("/", ""));
        return new S3Client(config);
    }

}
