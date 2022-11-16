package dev.soffa.foundation.commons.graphql;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.ErrorUtil;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unchecked")
public class GraphQLUtil {

    private GraphQLUtil() {}

    public static Map<String,Object> extractVariables(String payload) {
        GraphQLRequest request = Mappers.JSON_DEFAULT.deserialize(payload, GraphQLRequest.class);
        return flatten(request.getVariables());
    }

    private static Map<String,Object> flatten(Map<String, Object> input) {
        if (input==null || input.isEmpty()) {
            return input;
        }
        Map<String,Object> output = new HashMap<>();
        input.forEach((key, value) -> {
            if (value instanceof Map) {
                Map<String,Object> it = flatten((Map<String,Object>)value);
                if (it!=null) {
                    output.putAll(it);
                }
            } else if (output.containsKey(key)) {
                output.put(key.toLowerCase(Locale.ROOT) + "_" + output.keySet().size(), value);
            }else {
                output.put(key.toLowerCase(Locale.ROOT), value);
            }
        });
        return output;
    }

    public static String extractOperationName(String payload) {
        try {
            GraphQLRequest gql = Mappers.JSON_DEFAULT.deserialize(payload, GraphQLRequest.class);
            if (TextUtil.isNotEmpty(gql.getOperationName())) {
                return gql.getOperationName();
            }
            Document doc = new Parser().parseDocument(gql.getQuery());
            String operationName = null;
            for (Definition def : doc.getDefinitions()) {
                if (def instanceof OperationDefinition) {
                    operationName = ((OperationDefinition) def).getName();
                    if (TextUtil.isEmpty(operationName)) {
                        Field field = (Field)((OperationDefinition) def).getSelectionSet().getSelections().get(0);
                        operationName = field.getName();
                    }
                    if (TextUtil.isNotEmpty(operationName)) {
                        break;
                    }
                }
            }
            return operationName;
        } catch (Exception e) {
            Logger.app.error("Error while extracting operation name from payload: %s", ErrorUtil.getError(e));
            return null;
        }

    }

}
