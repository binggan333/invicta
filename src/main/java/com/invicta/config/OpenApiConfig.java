package com.invicta.config;

import com.invicta.common.ApiError;
import com.invicta.common.ApiErrorCode;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.invicta.common.ApiErrorCode.*;

@Configuration
public class OpenApiConfig {
    private static final Set<String> PROFILE_ALLOWED_PATHS = Set.of(
            "/api/v1/auth/me", "/api/v1/auth/profile", "/api/v1/organization/me");
    private static final Set<String> LEGACY_RESULT_PATHS = Set.of(
            "/api/v1/balance/create", "/api/v1/summary/create", "/api/v1/summary/check-status",
            "/api/v1/withdraw/apply", "/api/v1/withdraw/approve", "/api/v1/withdraw/proposal/confirm",
            "/api/v1/strategy/summary", "/api/v1/strategy/withdraw", "/api/v1/strategy/balance");

    @Bean
    OpenAPI invictaOpenApi() {
        String scheme = "bearerAuth";
        Components components = new Components()
                .addSecuritySchemes(scheme,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .description("登录后取得的 JWT，格式：Bearer <token>"));
        ModelConverters.getInstance().read(ApiError.class).forEach(components::addSchemas);
        return new OpenAPI()
                .info(new Info().title("Invicta API 文档").version("v1")
                        .description("Invicta 统计与看板接口。查询直接读取只读数据库，所有写操作由 UTC Pay 执行。"
                                + "\n\n成功统一返回 HTTP 200，Invicta 自身响应没有统一的 `code: 200` 包装。"
                                + "部分委托 UTC Pay 的写接口直接返回其 `{code,msg,data}` 结构，"
                                + "此时 HTTP 200 只代表请求已处理，响应体数字 `code=200` 才代表业务成功。"
                                + "\n\n错误响应为 `{code,message,timestamp,requestId,details}`；"
                                + "前端应先判断 HTTP 状态，再根据字符串 `code` 做具体处理。"
                                + errorCodeCatalog()))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(components);
    }

    /**
     * 根据实际异常处理路径为每个接口列出成功状态与可能的错误码。
     */
    @Bean
    OpenApiCustomizer standardErrorResponses() {
        return openApi -> openApi.getPaths().forEach((path, item) -> item.readOperations().forEach(operation -> {
            ApiResponse success = operation.getResponses().get("200");
            if (success == null) {
                success = new ApiResponse();
                operation.getResponses().addApiResponse("200", success);
            }
            success.setDescription(LEGACY_RESULT_PATHS.contains(path)
                    ? "请求已处理；响应体为 UTC Pay 结果，数字 code=200 才表示业务成功"
                    : path.endsWith("/flow/export") ? "成功导出 XLSX 文件" : "成功");

            List<ApiErrorCode> codes = new ArrayList<>(List.of(INVALID_REQUEST, VALIDATION_FAILED,
                    INTERNAL_SERVER_ERROR));
            boolean publicOperation = operation.getSecurity() != null && operation.getSecurity().isEmpty();
            if (!publicOperation) {
                codes.add(AUTHENTICATION_REQUIRED);
                codes.add(FORBIDDEN);
                if (!PROFILE_ALLOWED_PATHS.contains(path)) {
                    codes.add(PROFILE_INCOMPLETE);
                }
            }
            codes.addAll(specificErrorCodes(path));
            Map<Integer, List<ApiErrorCode>> byStatus = codes.stream().distinct().collect(Collectors.groupingBy(
                    code -> code.status().value(), LinkedHashMap::new, Collectors.toList()));
            byStatus.forEach((status, errors) -> operation.getResponses().addApiResponse(
                    Integer.toString(status), errorResponse(errors.stream()
                            .map(code -> "`" + code.code() + "`：" + code.meaning())
                            .collect(Collectors.joining("；")))));
        }));
    }

    private List<ApiErrorCode> specificErrorCodes(String path) {
        return switch (path) {
            case "/api/v1/auth/email/code" -> List.of(ACCOUNT_UNAVAILABLE, EMAIL_ALREADY_REGISTERED,
                    EXISTING_ACCOUNT_REQUIRED, CODE_REQUEST_TOO_FREQUENT, VERIFICATION_EMAIL_UNAVAILABLE);
            case "/api/v1/auth/email/login" -> List.of(ACCOUNT_UNAVAILABLE, VERIFICATION_CODE_INVALID);
            case "/api/v1/auth/existing/code" -> List.of(EXISTING_ACCOUNT_UNAVAILABLE,
                    CODE_REQUEST_TOO_FREQUENT, VERIFICATION_EMAIL_UNAVAILABLE);
            case "/api/v1/auth/existing/login" -> List.of(EXISTING_ACCOUNT_UNAVAILABLE,
                    VERIFICATION_CODE_INVALID);
            case "/api/v1/auth/password/login" -> List.of(INVALID_CREDENTIALS);
            case "/api/v1/auth/register" -> List.of(EMAIL_ALREADY_REGISTERED, EXISTING_ACCOUNT_REQUIRED,
                    VERIFICATION_CODE_INVALID, ACCOUNT_UNAVAILABLE);
            case "/api/v1/auth/profile" -> List.of(ACCOUNT_UNAVAILABLE);
            case "/api/v1/payment/detail" -> List.of(PAYMENT_NOT_FOUND);
            case "/api/v1/recharge/detail" -> List.of(RECHARGE_NOT_FOUND);
            case "/api/v1/organization/me" -> List.of(ORGANIZATION_NOT_FOUND);
            case "/api/v1/summary/detail" -> List.of(SUMMARY_NOT_FOUND);
            case "/api/v1/balance/detail" -> List.of(BALANCE_NOT_FOUND);
            case "/api/v1/withdraw/detail" -> List.of(WITHDRAW_NOT_FOUND);
            case "/api/v1/prepaid/account" -> List.of(PREPAID_ACCOUNT_NOT_FOUND);
            case "/api/v1/strategy/info" -> List.of(STRATEGY_NOT_FOUND);
            case "/api/v1/prepaid/flow/export" -> List.of(EXPORT_TIME_ORDER_INVALID,
                    EXPORT_RANGE_EXCEEDED, EXPORT_COUNT_EXCEEDED);
            default -> List.of();
        };
    }

    private String errorCodeCatalog() {
        String rows = java.util.Arrays.stream(ApiErrorCode.values())
                .sorted(Comparator.comparingInt(code -> code.status().value()))
                .map(code -> "\n| " + code.status().value() + " | `" + code.code() + "` | " + code.meaning() + " |")
                .collect(Collectors.joining());
        return "\n\n### 错误码总览\n\n| HTTP | code | 含义 |\n|---:|---|---|" + rows;
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse().description(description)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new io.swagger.v3.oas.models.media.Schema<>()
                                .$ref("#/components/schemas/ApiError"))));
    }

}
