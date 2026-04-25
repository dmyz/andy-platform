package net.junanw.upms.foundation.platform.auth.application.session.mapper;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.foundation.platform.auth.application.session.entity.AuthSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证会话 Mapper。
 */
@Mapper
public interface AuthSessionMapper extends BaseMapper<AuthSessionEntity> {
}
