package dev.soffa.foundation.commons;

import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;


public final class BeanUtil {

    private BeanUtil() {
    }

    @SneakyThrows
    public static <T> T copyProperties(Object src, T dest) {
        BeanUtils.copyProperties(dest, src);
        return dest;
    }

}
