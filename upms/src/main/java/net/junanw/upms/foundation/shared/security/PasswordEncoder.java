package net.junanw.upms.foundation.shared.security;

/**
 * 密码编码器接口
 */
public interface PasswordEncoder {

    /**
     * 对原始密码进行编码
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码
     */
    String encode(CharSequence rawPassword);

    /**
     * 验证原始密码是否与编码后的密码匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return 如果匹配返回 true，否则返回 false
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
