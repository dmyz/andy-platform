package net.junanw.upms.support.audit.login.repository;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.support.audit.login.entity.LoginAuditEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录审计事件 Mapper 接口。
 *
 * <p>提供登录审计事件的条件查询与持久化能力。
 */
@Mapper
public interface LoginAuditEventMapper extends BaseMapper<LoginAuditEventEntity> {
}
