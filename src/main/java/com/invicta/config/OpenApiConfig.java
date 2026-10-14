package com.invicta.config;

import com.invicta.common.ApiError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI invictaOpenApi() {
        String scheme = "bearerAuth";
        Components components = new Components()
                .addSecuritySchemes(scheme,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .description("登录后取得的 JWT，格式：Bearer <token>"));
        ModelConverters.getInstance().read(ApiError.class).forEach(components::addSchemas);
        components.addResponses("BadRequest", errorResponse("请求参数不正确"))
                .addResponses("Unauthorized", errorResponse("未登录或令牌无效"))
                .addResponses("Forbidden", errorResponse("没有目标资源的访问权限"))
                .addResponses("InternalServerError", errorResponse("服务内部错误"));

        return new OpenAPI()
                .info(new Info().title("Invicta API 文档").version("v1")
                        .description("Invicta 统计与看板接口。查询直接读取只读数据库，所有写操作由 UTC Pay 执行。"))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(components);
    }

    /**
     * 为所有接口补充统一错误响应；公开认证接口不展示 401 和 403。
     */
    @Bean
    OpenApiCustomizer standardErrorResponses() {
        return openApi -> openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
            operation.getResponses().addApiResponse("400", responseReference("BadRequest"));
            operation.getResponses().addApiResponse("500", responseReference("InternalServerError"));
            boolean publicOperation = operation.getSecurity() != null && operation.getSecurity().isEmpty();
            if (!publicOperation) {
                operation.getResponses().addApiResponse("401", responseReference("Unauthorized"));
                operation.getResponses().addApiResponse("403", responseReference("Forbidden"));
            }
        }));
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse().description(description)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new io.swagger.v3.oas.models.media.Schema<>()
                                .$ref("#/components/schemas/ApiError"))));
    }

    private ApiResponse responseReference(String name) {
        return new ApiResponse().$ref("#/components/responses/" + name);
    }
}
