package net.junanw.upms.foundation.platform.web.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.support.audit.operation.service.OperationAuditRecord;
import net.junanw.upms.support.audit.operation.service.OperationAuditService;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 管理端操作审计过滤器。
 *
 * <p>该过滤器拦截需要记录审计的管理端写操作请求，提取请求摘要、响应摘要、执行结果和操作人信息，
 * 最终写入操作审计服务。
 */
@Component
public class OperationAuditFilter extends OncePerRequestFilter {

    /** 摘要最大长度，避免审计日志无限膨胀。 */
    private static final int MAX_SUMMARY_LENGTH = 2_000;

    /** 请求体缓存上限，避免大包体无限缓存。 */
    private static final int MAX_CACHE_LENGTH = 8_192;

    /** 需要脱敏的敏感字段模式。 */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)\"(oldPassword|newPassword|password|confirmPassword|captcha)\"\s*:\s*\"[^\"]*\"");

    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();
    private final OperationAuditService operationAuditService;
    private final LoginUserContext loginUserContext;

    public OperationAuditFilter(OperationAuditService operationAuditService, LoginUserContext loginUserContext) {
        this.operationAuditService = operationAuditService;
        this.loginUserContext = loginUserContext;
    }

    /**
     * 执行过滤与审计记录。
     *
     * <p>过滤器始终先放行业务处理，再根据结果在 finally 中尝试写审计，
     * 以确保成功与失败请求都能尽量留下轨迹，并且不会影响响应体正常返回。
     *
     * @param request 原始请求
     * @param response 原始响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_CACHE_LENGTH);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        String actorUsername = loginUserContext.getLoginUsernameDefaultNull();
        long startedAt = System.currentTimeMillis();
        Exception failure = null;

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        }
        catch (ServletException | IOException | RuntimeException exception) {
            failure = exception;
            throw exception;
        }
        finally {
            try {
                if (shouldRecord(requestWrapper, actorUsername)) {
                    recordEvent(requestWrapper, responseWrapper, actorUsername, startedAt, failure);
                }
            }
            finally {
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    /**
     * 判定当前请求是否应跳过审计。
     *
     * <p>仅记录管理端非 GET 写操作，并继续排除登录与发码等高频或已有专门审计链路的接口。
     *
     * @param request 原始请求
     * @return {@code true} 表示跳过过滤
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/admin/")
                || HttpMethod.GET.matches(request.getMethod())
                || "/admin/auth/login".equals(uri)
                || uri.startsWith("/admin/auth/code/");
    }

    /**
     * 判定当前请求是否值得写入审计。
     *
     * @param request 请求包装器
     * @param actorUsername 当前用户名
     * @return 是否记录
     */
    private boolean shouldRecord(ContentCachingRequestWrapper request, String actorUsername) {
        return actorUsername != null && !actorUsername.isBlank() && !request.getRequestURI().startsWith("/admin/operation-audit");
    }

    /**
     * 组装并提交操作审计记录。
     *
     * @param request 请求包装器
     * @param response 响应包装器
     * @param actorUsername 操作用户名
     * @param startedAt 请求开始时间戳
     * @param failure 执行期间抛出的异常
     */
    private void recordEvent(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            String actorUsername,
            long startedAt,
            Exception failure
    ) {
        String requestUri = request.getRequestURI();
        String responseSummary = resolveResponseSummary(response, failure);
        String resultStatus = resolveResultStatus(responseSummary, response.getStatus(), failure);
        operationAuditService.record(new OperationAuditRecord(
                actorUsername,
                resolveModuleCode(requestUri),
                resolveActionCode(request),
                resolveTargetType(requestUri),
                resolveTargetId(requestUri),
                request.getMethod(),
                requestUri,
                resolveRequestSummary(request),
                responseSummary,
                resultStatus,
                Math.max(System.currentTimeMillis() - startedAt, 0L),
                request.getRemoteAddr(),
                request.getHeader("X-Trace-Id"),
                request.getHeader("X-Request-Id"),
                LocalDateTime.now()
        ));
    }

    /**
     * 解析模块编码。
     *
     * @param requestUri 请求路径
     * @return 模块编码
     */
    private String resolveModuleCode(String requestUri) {
        String[] segments = requestUri.split("/");
        return segments.length > 3 ? segments[3].trim().toLowerCase(Locale.ROOT) : "unknown";
    }

    /**
     * 根据请求特征推断动作编码。
     *
     * @param request 请求包装器
     * @return 动作编码
     */
    private String resolveActionCode(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        if (uri.endsWith("/offline")) {
            return "OFFLINE";
        }
        if (uri.endsWith("/publish")) {
            return "PUBLISH";
        }
        if (uri.endsWith("/withdraw")) {
            return "WITHDRAW";
        }
        if (uri.endsWith("/assign") || uri.contains("/assign")) {
            return uri.contains("permission") ? "ASSIGN_PERMISSION" : "ASSIGN";
        }
        if (uri.contains("password")) {
            return uri.contains("reset") ? "RESET_PASSWORD" : "CHANGE_PASSWORD";
        }
        if (uri.contains("upload")) {
            return "UPLOAD";
        }
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    /**
     * 解析审计目标类型。
     *
     * @param requestUri 请求路径
     * @return 目标类型
     */
    private String resolveTargetType(String requestUri) {
        String moduleCode = resolveModuleCode(requestUri);
        return moduleCode.equals("unknown") ? null : moduleCode.toUpperCase(Locale.ROOT);
    }

    /**
     * 从 URL 尾段中推断目标 ID。
     *
     * @param requestUri 请求路径
     * @return 目标 ID；未识别时返回 {@code null}
     */
    private Long resolveTargetId(String requestUri) {
        String[] segments = requestUri.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            if (segment != null && segment.matches("[0-9]+")) {
                try {
                    return Long.valueOf(segment);
                }
                catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 生成请求摘要。
     *
     * <p>认证类请求直接隐藏，请求体中的敏感字段统一脱敏，多段或超长内容会被截断。
     *
     * @param request 请求包装器
     * @return 请求摘要
     */
    private String resolveRequestSummary(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        String contentType = request.getContentType();
        if (uri.startsWith("/admin/auth/")) {
            return "[sensitive request omitted]";
        }
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
            return "[multipart request omitted]";
        }
        String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8).trim();
        if (body.isEmpty()) {
            body = request.getQueryString() == null ? "" : request.getQueryString();
        }
        return truncate(maskSensitive(body));
    }

    /**
     * 生成响应摘要。
     *
     * @param response 响应包装器
     * @param failure 运行异常
     * @return 响应摘要
     */
    private String resolveResponseSummary(ContentCachingResponseWrapper response, Exception failure) {
        if (failure != null) {
            return truncate(defaultString(failure.getMessage()));
        }
        return truncate(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8).trim());
    }

    /**
     * 根据 HTTP 结果与业务响应体解析审计状态。
     *
     * @param responseSummary 响应摘要
     * @param responseStatus HTTP 状态码
     * @param failure 执行异常
     * @return SUCCESS 或 FAIL
     */
    private String resolveResultStatus(String responseSummary, int responseStatus, Exception failure) {
        if (failure != null || responseStatus >= 400) {
            return "FAIL";
        }
        Map<String, Object> payload = parseJson(responseSummary);
        Object code = payload.get("code");
        if (code instanceof Number number) {
            return number.intValue() == 0 ? "SUCCESS" : "FAIL";
        }
        return "SUCCESS";
    }

    /**
     * 尝试解析 JSON 文本。
     *
     * @param json JSON 字符串
     * @return Map 结构；无法解析时返回空 Map
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return jsonParser.parseMap(json);
        }
        catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    /**
     * 脱敏敏感字段。
     *
     * @param value 原始字符串
     * @return 脱敏后的字符串
     */
    private String maskSensitive(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return PASSWORD_PATTERN.matcher(value).replaceAll("\"$1\":\"***\"");
    }

    /**
     * 截断超长摘要。
     *
     * @param value 原始字符串
     * @return 截断后的结果
     */
    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_SUMMARY_LENGTH ? value : value.substring(0, MAX_SUMMARY_LENGTH);
    }

    /**
     * 将空字符串安全转为默认值。
     *
     * @param value 原始值
     * @return 非空字符串
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
