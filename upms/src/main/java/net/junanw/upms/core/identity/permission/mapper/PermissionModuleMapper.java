package net.junanw.upms.core.identity.permission.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.identity.permission.entity.PermissionModuleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限模块目录 Mapper 接口。
 *
 * <p>提供权限模块目录的查询和维护能力。
 */
@Mapper
public interface PermissionModuleMapper extends MybatisMapper<PermissionModuleEntity> {
}
