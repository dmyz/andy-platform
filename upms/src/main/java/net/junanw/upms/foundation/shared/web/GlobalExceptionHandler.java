package net.junanw.upms.foundation.shared.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 全局异常处理器。
 *
 * <p>统一把业务异常、参数校验异常和鉴权异常转换为标准 API 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 处理业务异常。 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return failure(exception.getCode(), exception.getMessage());
    }

    /** 处理请求体校验异常。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return failure(400, joinFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    /** 处理绑定异常。 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return failure(400, joinFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    /** 处理常见坏请求异常。 */
    @ExceptionHandler({
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(Exception exception) {
        return failure(400, exception.getMessage());
    }

    /** 处理未登录异常。 */
    /** 处理资源不存在异常。 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException exception) {
        return failure(404, "请求资源不存在");
    }

    /** 处理未登录异常。 */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException exception) {
        return failure(401, "未登录或会话失效");
    }

    /** 处理无权限异常。 */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public ResponseEntity<ApiResponse<Void>> handlePermissionException(SaTokenException exception) {
        return failure(403, "无权限访问");
    }

    /** 处理 Sa-Token 其他异常。 */
    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleSaTokenException(SaTokenException exception) {
        return failure(401, exception.getMessage());
    }

    /** 处理兜底系统异常。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(HttpServletRequest request, Exception exception) {
        log.error("系统异常: {}", request.getRequestURI(), exception);
        return failure(500, "系统异常");
    }

    /** 构造失败响应。 */
    private ResponseEntity<ApiResponse<Void>> failure(int status, String message) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.failure(status, message));
    }

    /** 拼接字段错误消息。 */
    private String joinFieldErrors(Iterable<FieldError> errors) {
        return toStream(errors)
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

    /** 把字段错误集合转为 Stream。 */
    private Stream<FieldError> toStream(Iterable<FieldError> errors) {
        return java.util.stream.StreamSupport.stream(errors.spliterator(), false);
    }
}
