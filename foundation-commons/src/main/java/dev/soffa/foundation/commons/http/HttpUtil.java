package dev.soffa.foundation.commons.http;


import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.http.mocks.HttpMock;
import lombok.SneakyThrows;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public final class HttpUtil {

    private static final List<Interceptor> INTERCEPTORS = new ArrayList<>();
    private static final Logger LOG = Logger.get(HttpUtil.class);

    private HttpUtil() {
    }

    public static String createBasicAuthorization(String username, String password) {
        final String pair = username + ":" + password;
        final String encoded = Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    @SneakyThrows
    public static OkHttpClient newOkHttpClient() {
        List<String> candidates = Arrays.asList("http_proxy", "https_proxy", "HTTP_PROXY", "HTTPS_PROXY");
        for (String candidate : candidates) {
            String value = System.getenv(candidate);
            if (TextUtil.isNotEmpty(value)) {
                return newOkHttpClient(value, true);
            }
        }
        return newOkHttpClient(null, true);
    }

    @SneakyThrows
    public static OkHttpClient newOkHttpClient(boolean trustAll) {
        return newOkHttpClient(null, trustAll);
    }

    @SneakyThrows
    public static OkHttpClient newOkHttpClient(String proxy, boolean trustAll) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true);

        if (trustAll) {
            LOG.debug("HttpClient is configured to trust all certificates");
            final TrustManager[] trustAllCerts = {new TrustAllManager()};
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
        }

        if (TextUtil.isNotEmpty(proxy)) {
            LOG.debug("HttpClient is using proxy: %s", proxy);
            URL parsedUrl = new URL(proxy);
            Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parsedUrl.getHost(), parsedUrl.getPort()));
            String userInfo = parsedUrl.getUserInfo();
            builder.proxy(p);
            if (TextUtil.isNotEmpty(userInfo)) {
                String[] parts = userInfo.split(":");
                String credential = Credentials.basic(parts[0], parts[1]);
                builder.proxyAuthenticator((route, response) -> {
                    // Emty Line
                    return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
                });
            }
        }

        builder.addNetworkInterceptor(chain -> {
            Request originalRequest = chain.request();
            String contentType = originalRequest.header("Content-Type");
            if (contentType == null || TextUtil.isEmpty(contentType) || contentType.contains("application/json")) {
                contentType = "application/json"; // Because OkHttp adds ;charset-utf8
            }
            Request.Builder request = originalRequest.newBuilder().header("Content-Type", contentType);

            HttpContextHolder.get().ifPresent(context -> {
                for (Map.Entry<String, String> e : context.entrySet()) {
                    boolean isHeaderMissing = TextUtil.isEmpty(originalRequest.header(e.getKey()));
                    if (isHeaderMissing && TextUtil.isNotEmpty(e.getValue())) {
                        request.header(e.getKey(), e.getValue());
                    }
                }
            });
            return chain.proceed(request.build());
        });


        for (Interceptor interceptor : INTERCEPTORS) {
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

    @SneakyThrows
    public static void loadMocks(String path) {
        try (InputStream input = HttpUtil.class.getResourceAsStream(path)) {
            List<HttpMock> mocks = Mappers.YAML.deserializeList(input, HttpMock.class);
            addInterceptor(chain -> {
                Request request = chain.request();
                for (HttpMock mock : mocks) {
                    if (mock.matches(request.url().url(), HttpHeaders.of(chain.request().headers()))) {
                        return handleRequest(request, mock);
                    }
                }
                return chain.proceed(request);
            });
        }
    }

    public static void mockResponse(BiFunction<URL, HttpHeaders, Boolean> delegate, HttpResponseProvider handler) {
        addInterceptor(chain -> {
            Request request = chain.request();
            if (!delegate.apply(request.url().url(), HttpHeaders.of(chain.request().headers()))) {
                return chain.proceed(request);
            }
            return handleRequest(request, handler);
        });
    }

    public static void mockResponse(HttpResponseProvider handler) {
        addInterceptor(chain -> {
            Request request = chain.request();
            return handleRequest(request, handler);
        });
    }


    private static Response handleRequest(Request request, HttpResponseProvider handler) {
        HttpResponse res = handler.apply(request.url().url(), HttpHeaders.of(request.headers()));
        MediaType contentType = MediaType.parse(Optional.ofNullable(res.getContentType()).orElse("application/json"));
        return new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(res.getStatus())
            .message("Mocked response")
            .body(ResponseBody.create(res.getBody(), contentType))
            .build();
    }

    public static void addInterceptor(Interceptor interceptor) {
        INTERCEPTORS.add(interceptor);
    }

}
