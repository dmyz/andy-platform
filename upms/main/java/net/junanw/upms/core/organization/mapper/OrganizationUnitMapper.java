package net.junanw.upms.core.organization.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.organization.entity.OrganizationUnitEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组织单元 Mapper 接口。
 *
 * <p>提供组织节点查询、组织树装载和子节点存在性判断能力。
 */
@Mapper
public interface OrganizationUnitMapper extends MybatisMapper<OrganizationUnitEntity> {
}
