package net.junanw.upms.core.organization.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.organization.entity.OrganizationMembershipEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组织成员关系 Mapper 接口。
 *
 * <p>提供用户组织关系的批量装载、主组织查询和清理能力。
 */
@Mapper
public interface OrganizationMembershipMapper extends MybatisMapper<OrganizationMembershipEntity> {
}
