package dev.soffa.foundation.openapi;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.resource.OpenApi;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OpenApiBuilder {

    private final Components components = new Components();
    private final OpenAPIDesc desc;

    public OpenApiBuilder(OpenAPIDesc desc) {
        this.desc = desc;
    }

    public OpenAPI build() {
        String version = "3.0.1";
        OpenAPI api;
        if (desc != null) {
            version = Optional.ofNullable(desc.getVersion()).orElse(version);
            api = new OpenAPI().openapi(version);
            if (desc.getSecurity() != null) {
                buildOAuthSchemes();
                buildBearerScheme();
                buildBasicAuthScheme();
            }
            api.setInfo(buildInfo());
            buildParameters();
        } else {
            api = new OpenAPI().openapi(version);
        }
        addServers(api);
        addHealthCheck(api);
        api.setComponents(components);

        return api;
    }

    private void addHealthCheck(OpenAPI api) {
        api.addTagsItem(new Tag().name("Health").description("Service healthcheck"));
        Operation op = new Operation()
            .addTagsItem("Health")
            .summary("Returns the health status of the service")
            .operationId("healthCheck");

        MediaType mediaType = new MediaType().schema(new Schema<Map<String, Object>>().type("object"));
        Content content = new Content()
            .addMediaType("application/json", mediaType);

        ApiResponses responses = new ApiResponses()
            .addApiResponse("200", new ApiResponse().content(content));
        op.setResponses(responses);

        PathItem path = new PathItem().get(op);
        api.path("/health", path);
    }

    private void addServers(OpenAPI api) {
        if (TextUtil.isEmpty(desc.getServers())) {
            Logger.platform.info("No OpenAPI servers defined, using default");
            return;
        }
        List<io.swagger.v3.oas.models.servers.Server> servers = new ArrayList<>();
        for (String server : desc.getServers().split(",")) {
            servers.add(new Server().url(server));
        }
        api.setServers(servers);
    }

    private Info buildInfo() {
        Info info = new Info();
        if (desc.getInfo() != null) {
            info.setTitle(desc.getInfo().getTitle());
            info.setDescription(desc.getInfo().getDescription());
            info.setVersion(desc.getInfo().getVersion());
            info.setContact(desc.getInfo().getContact());
        }
        return info;
    }

    private void buildOAuthSchemes() {
        if (desc.getSecurity().getOAuth2() == null) {
            return;
        }
        Logger.platform.info("[security] oAuth is enabled");

        Scopes scopes = new Scopes();
        int flows = 0;
        OAuthFlows oAuthFlows = new OAuthFlows();

        OpenAPIDesc.OAuth2 oauth2 = desc.getSecurity().getOAuth2();

        if (oauth2.isAuthorizationCodeFlow()) {
            flows++;
            OAuthFlow passwordFlow = new OAuthFlow();
            passwordFlow.setTokenUrl(oauth2.getUrl() + "/token");
            passwordFlow.setScopes(scopes);
            oAuthFlows.setPassword(passwordFlow);
        }

        if (flows > 0) {
            components.addSecuritySchemes(OpenApi.OAUTH2,
                new SecurityScheme()
                    .description("OAuth2 OpenID Connect")
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(oAuthFlows)
            );
        }
    }

    private void buildBearerScheme() {
        if (!desc.getSecurity().isBearerAuth()) {
            return;
        }
        Logger.platform.info("[security] BearerAuth is enabled");
        components.addSecuritySchemes(OpenApi.BEARER_AUTH,
            new SecurityScheme()
                .description("Bearer Auth")
                .scheme("bearer")
                .bearerFormat("Bearer [token]")
                .type(SecurityScheme.Type.HTTP)
        );

    }

    private void buildBasicAuthScheme() {
        if (!desc.getSecurity().isBasicAuth()) {
            return;
        }
        Logger.platform.info("[security] BasicAuth is enabled");
        components.addSecuritySchemes(OpenApi.BASIC_AUTH,
            new SecurityScheme()
                .description("Basic Auth")
                .scheme("basic")
                .type(SecurityScheme.Type.HTTP)
        );

    }

    private void buildParameters() {

        if (desc.getParameters() == null || desc.getParameters().isEmpty()) {
            return;
        }

        for (OpenAPIDesc.Parameter param : desc.getParameters()) {
            String name = param.getName();

            Preconditions.checkArgument(TextUtil.isNotEmpty(name), "openapi parameter name or ref is required");
            Preconditions.checkArgument(TextUtil.isNotEmpty(param.getIn()), "openapi parameter.in is required");

            Parameter parameter = new Parameter();
            parameter.setIn(param.getIn().toLowerCase());
            parameter.setDescription(param.getDescription());
            parameter.setName(name);
            //parameter.setAllowEmptyValue(param.isNullable());
            parameter.setRequired(param.isRequired());

            Schema<String> schema = new Schema<>();
            schema.setType(param.getType());
            //schema.setNullable(param.isNullable());

            if (CollectionUtil.isNotEmpty(param.getValues())) {
                schema.setEnum(param.getValues());
                schema.setDefault(param.getValue());
                parameter.setSchema(schema);
            } else if (TextUtil.isNotEmpty(param.getValue())) {
                schema.setDefault(param.getValue());
            }
            parameter.setSchema(schema);
            components.addParameters(name, parameter);
        }

    }
}
