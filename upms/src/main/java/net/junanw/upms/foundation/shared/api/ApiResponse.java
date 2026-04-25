package net.junanw.upms.foundation.shared.api;

/**
 * 统一接口响应体。
 *
 * @param code 业务状态码
 * @param message 响应消息
 * @param data 业务数据
 * @param <T> 数据类型
 */
public record ApiResponse<T>(int code, String message, T data) {

    /** 创建成功响应。 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /** 创建带消息的成功响应。 */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    /** 创建失败响应。 */
    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
