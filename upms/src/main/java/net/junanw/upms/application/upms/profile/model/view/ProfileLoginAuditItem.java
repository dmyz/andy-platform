package net.junanw.upms.application.upms.profile.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 个人登录审计项视图。
 *
 * <p>用于承载个人中心登录审计列表的返回字段。
 *
 * @param id 审计记录 ID
 * @param loginTime 登录时间
 * @param loginType 登录方式
 * @param ip 请求来源 IP
 * @param browser 浏览器信息摘要
 * @param result 登录结果
 */

public record ProfileLoginAuditItem(
        String id,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime loginTime,
        String loginType,
        String ip,
        String browser,
        String result
) {
}
