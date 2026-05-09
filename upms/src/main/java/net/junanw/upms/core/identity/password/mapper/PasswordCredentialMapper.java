package net.junanw.upms.core.identity.password.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.identity.password.entity.PasswordCredentialEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 密码凭证 Mapper。
 * <p>
 * 对外提供以用户为中心的密码凭证读取能力，
 * 供登录校验、改密、重置密码等场景复用。
 */
@Mapper
public interface PasswordCredentialMapper extends MybatisMapper<PasswordCredentialEntity> {
}
