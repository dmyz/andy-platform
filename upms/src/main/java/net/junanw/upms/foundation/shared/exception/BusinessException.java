package net.junanw.upms.foundation.shared.exception;

/**
 * 业务异常。
 *
 * <p>用于向上层返回可预期的业务错误码和错误信息。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /** 使用默认 400 状态码创建业务异常。 */
    public BusinessException(String message) {
        this(400, message);
    }

    /** 使用指定状态码创建业务异常。 */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 返回业务错误码。 */
    public int getCode() {
        return code;
    }
}
