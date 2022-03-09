package dev.soffa.foundation.test.karate;

import dev.soffa.foundation.commons.http.HttpUtil;

public final class KarateTestUtil {

    private KarateTestUtil() {
    }

    public static String basicAuth(String username, String pasword) {
        return HttpUtil.createBasicAuthorization(username, pasword);
    }

}
