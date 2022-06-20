package dev.soffa.foundation.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.soffa.foundation.annotation.Hateos;
import dev.soffa.foundation.commons.JacksonMapper;
import dev.soffa.foundation.model.HateosLink;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class HateosEntityControllerAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper mapper;

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
        Map<String,Object> transformed = JacksonMapper.toMap(mapper, body, Object.class);
        transformed.put("_links", links);
        return transformed;
    }
}
