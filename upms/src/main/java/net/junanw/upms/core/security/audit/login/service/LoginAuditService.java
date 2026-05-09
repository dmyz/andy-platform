package net.junanw.upms.core.security.audit.login.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.security.audit.login.model.view.LoginAuditPageItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录审计服务接口。
 *
 * <p>定义登录事件记录、分页查询和导出能力。
 */
public interface LoginAuditService {

    /**
     * 记录登录审计事件。
     *
     * @param command 登录审计命令
     */
    default void record(LoginAuditRecord command) {
    }

    /**
     * 查询登录审计分页。
     *
     * @param username 用户名关键字
     * @param loginType 登录方式
     * @param result 结果筛选
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResponse<LoginAuditPageItem> page(
            String username,
            String loginType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    );

    /**
     * 导出登录审计列表。
     *
     * @param username 用户名关键字
     * @param loginType 登录方式
     * @param result 结果筛选
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return 导出结果列表
     */
    List<LoginAuditPageItem> export(
            String username,
            String loginType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
