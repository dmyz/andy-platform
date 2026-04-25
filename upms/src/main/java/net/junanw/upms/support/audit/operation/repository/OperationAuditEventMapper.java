package net.junanw.upms.support.audit.operation.repository;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.support.audit.operation.entity.OperationAuditEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作审计事件 Mapper 接口。
 *
 * <p>提供操作审计事件的条件查询与持久化能力。
 */
@Mapper
public interface OperationAuditEventMapper extends BaseMapper<OperationAuditEventEntity> {
}
