package dev.soffa.foundation.commons;


public final class JavaUtil {

    private JavaUtil() {
    }

    public static boolean isJava8() {
        String versions = System.getProperty("java.version").toString();
        int version = Integer.parseInt(System.getProperty("java.version").split("\\.")[0]);
        return versions.startsWith("1.8") || version == 8;
    }
}
