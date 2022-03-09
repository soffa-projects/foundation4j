package dev.soffa.foundation.openapi;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.errors.TechnicalException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public class ApiInfo {

    private String method;
    private String path;


    public static Map<String, ApiInfo> of(Class<?> clientInterface) {
        Map<String, ApiInfo> infos = new HashMap<>();
        for (Method method : clientInterface.getMethods()) {
            Path path = method.getAnnotation(Path.class);
            Operation operation = method.getAnnotation(Operation.class);
            if (path == null || operation == null) {
                throw new TechnicalException("Method '%s' should be annotated with @Path and @BindOperation", method.getName());
            }
            if (TextUtil.isEmpty(path.value())) {
                throw new TechnicalException("@Path value is required on methid '%s'", method.getName());
            }
            if (TextUtil.isEmpty(operation.method())) {
                throw new TechnicalException("@Operationd.method is required on methid '%s'", method.getName());
            }
            infos.put(method.getName(), new ApiInfo(operation.method(), "/" + path.value().replaceAll("^/+", "")));
        }
        return infos;
    }

}
