package dev.soffa.foundation.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.soffa.foundation.annotation.Hateos;
import dev.soffa.foundation.commons.JacksonMapper;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.UrlUtil;
import dev.soffa.foundation.model.HateosLink;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.net.URI;
import java.util.Map;

@ControllerAdvice
public class HateosEntityControllerAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger LOG = Logger.getLogger(HateosEntityControllerAdvice.class);
    private final ObjectMapper mapper;

    private final String publicUrl;

    public HateosEntityControllerAdvice(ObjectMapper mapper, Environment env) {
        this.publicUrl = env.getRequiredProperty("app.public-url");
        this.mapper = mapper;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().isAnnotationPresent(Hateos.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NotNull MethodParameter returnType,
                                  @NotNull MediaType selectedContentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NotNull ServerHttpRequest request,
                                  @NotNull ServerHttpResponse response) {

        if (body == null || request.getMethod() != HttpMethod.GET) {
            return body;
        }

        Map<String, HateosLink> links = ImmutableMap.of(
            "self", new HateosLink(rewriteInternalLink(request.getURI()))
        );
        Map<String, Object> transformed = JacksonMapper.toMap(mapper, body, Object.class);
        transformed.put("_links", links);
        return transformed;
    }

    private String rewriteInternalLink(URI input) {
        if (publicUrl.contains("localhost")) {
            return input.toString();
        }
        return UrlUtil.join(publicUrl, input.getPath());
    }
}
