package net.junanw.upms.core.identity.authentication.model.request;

/**
 * 统一登录请求体。
 *
 * <p>封装统一登录接口的请求参数；控制器保持单一请求结构，内部再按登录方式转换为认证命令。
 *
 * @param grantType 登录方式，空值默认密码登录
 * @param username 用户名，密码登录时使用
 * @param password 登录密码，密码登录时使用
 * @param mobile 手机号，手机验证码登录时使用
 * @param email 邮箱地址，邮箱验证码登录时使用
 * @param code 验证码，验证码登录时使用
 */

public record UnifiedLoginRequest(
        String grantType,
        String username,
        String password,
        String mobile,
        String email,
        String code
) {
}
