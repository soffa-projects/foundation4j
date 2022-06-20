package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.annotation.Hateos;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.model.HateosLink;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@ControllerAdvice
public class HateosEntityControllerAdvice implements ResponseBodyAdvice<Object> {

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
        // Hateos ano = body.getClass().getAnnotation(Hateos.class);
        Map<String,HateosLink> links = ImmutableMap.of(
            "self", new HateosLink(request.getURI().toString())
        );
        Map<String,Object> transformed = Mappers.JSON.toMap(body);
        transformed.put("_links", links);
        return transformed;
    }
}
