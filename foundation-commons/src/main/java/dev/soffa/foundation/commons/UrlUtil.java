package dev.soffa.foundation.commons;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public final class UrlUtil {

    private UrlUtil() {
    }

    @SneakyThrows
    public static File download(String url) {
        URL lUrl = new URL(url);
        return download(lUrl, FilenameUtils.getExtension(lUrl.getFile()));
    }

    @SneakyThrows
    public static File download(URL url, String extension) {
        File localFile = File.createTempFile(UUID.randomUUID().toString(), extension);
        localFile.deleteOnExit();
        Logger.platform.info("Downloading %s to %s", url.toString(), localFile.getAbsolutePath());
        try (InputStream input = url.openStream()) {
            FileUtils.copyInputStreamToFile(input, localFile);
        }
        Logger.platform.info("Archive downloaded: %s", url.toString());
        return localFile;
    }

    public static String join(String base, String... parts) {
        StringBuilder url = new StringBuilder(base.replaceAll("/+$", ""));
        for (String part : parts) {
            url.append('/').append(part.replaceAll("^/+", ""));
        }
        return url.toString();
    }

}
