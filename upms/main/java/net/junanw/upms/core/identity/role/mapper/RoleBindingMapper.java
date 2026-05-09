package net.junanw.upms.core.identity.role.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.identity.role.entity.RoleBindingEntity;

/**
 * 角色绑定 Mapper 接口。
 *
 * <p>提供主体角色关系的查询与覆盖式删除能力。
 */
public interface RoleBindingMapper extends MybatisMapper<RoleBindingEntity> {
}
