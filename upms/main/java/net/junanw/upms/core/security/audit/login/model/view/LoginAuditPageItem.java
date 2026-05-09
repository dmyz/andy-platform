package net.junanw.upms.core.security.audit.login.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 登录审计分页项视图。
 *
 * <p>用于承载登录审计分页列表的返回字段。
 *
 * @param id 审计记录 ID
 * @param username 用户名
 * @param realName 用户姓名
 * @param loginType 登录方式
 * @param ip 请求来源 IP
 * @param browser 浏览器信息摘要
 * @param loginTime 登录时间
 * @param result 登录结果
 * @param failureReason 失败原因
 */

public record LoginAuditPageItem(
        String id,
        String username,
        String realName,
        String loginType,
        String ip,
        String browser,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime loginTime,
        String result,
        String failureReason
) {
}
