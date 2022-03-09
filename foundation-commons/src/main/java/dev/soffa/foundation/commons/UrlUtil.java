package dev.soffa.foundation.commons;

public final class UrlUtil {

    private UrlUtil() {
    }

    public static String join(String base, String... parts) {
        StringBuilder url = new StringBuilder(base.replaceAll("/+$", ""));
        for (String part : parts) {
            url.append('/').append(part.replaceAll("^/+", ""));
        }
        return url.toString();
    }

}
