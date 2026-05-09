package net.junanw.upms.core.identity.session.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.identity.session.entity.AuthSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证会话 Mapper。
 */
@Mapper
public interface AuthSessionMapper extends MybatisMapper<AuthSessionEntity> {
}
