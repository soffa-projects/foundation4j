package dev.soffa.foundation.commons;

import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public final class IOUtil {

    private IOUtil() {
    }

    @SneakyThrows
    public static Optional<String> readFileToString(File input) {
        if (input == null || !input.isFile() || !input.canRead()) {
            return Optional.empty();
        }
        String content = FileUtils.readFileToString(input, StandardCharsets.UTF_8);
        if (StringUtils.isBlank(content)) {
            return Optional.empty();
        }
        return Optional.of(content);
    }

    @SneakyThrows
    public static Optional<String> toString(InputStream input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.of(IOUtils.toString(input, StandardCharsets.UTF_8));
    }

    @SneakyThrows
    public static String toStringSafe(InputStream input) {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static String getResourceAsString(String path) {
        try (InputStream stream = IOUtil.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new TechnicalException("Resource not found: %s", path);
            }
            return IOUtil.toStringSafe(stream);
        }
    }

    @SneakyThrows
    public static void write(String data, File output) {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(output.getPath()))) {
            IOUtils.write(data, w);
            w.flush();
        }
    }


}
