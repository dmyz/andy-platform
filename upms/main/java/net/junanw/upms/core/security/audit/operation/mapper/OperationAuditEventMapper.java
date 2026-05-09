package net.junanw.upms.core.security.audit.operation.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.security.audit.operation.entity.OperationAuditEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作审计事件 Mapper 接口。
 *
 * <p>提供操作审计事件的条件查询与持久化能力。
 */
@Mapper
public interface OperationAuditEventMapper extends MybatisMapper<OperationAuditEventEntity> {
}
