package net.junanw.upms.foundation.platform.auth.authentication;

import net.junanw.upms.foundation.platform.auth.authentication.model.AuthGrantType;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;

/**
 * 统一登录认证命令。
 *
 * @param grantType 登录方式
 * @param username 用户名
 * @param password 密码明文
 * @param mobile 手机号
 * @param email 邮箱
 * @param code 验证码
 * @param details 请求上下文
 */
public record LoginAuthentication(
        AuthGrantType grantType,
        String username,
        String password,
        String mobile,
        String email,
        String code,
        AuthRequestContext details
) {

    /**
     * 提取当前登录方式对应的账号标识，用于审计与错误记录。
     */
    public String accountIdentifier() {
        return switch (grantType) {
            case PASSWORD -> username;
            case MOBILE_CODE -> mobile;
            case EMAIL_CODE -> email;
        };
    }
}
