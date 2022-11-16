package dev.soffa.foundation.commons.graphql;


import lombok.Data;

import java.util.Map;

@Data
public class GraphQLRequest {

    private String operationName;
    private String query;
    private Map<String,Object> variables;
}
