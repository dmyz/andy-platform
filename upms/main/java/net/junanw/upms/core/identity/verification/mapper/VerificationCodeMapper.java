package net.junanw.upms.core.identity.verification.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.identity.verification.entity.VerificationCodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码 Mapper。
 *
 * <p>负责查询某个目标在指定场景下最近一次未使用验证码，以及批量获取待失效历史验证码。
 */
@Mapper
public interface VerificationCodeMapper extends MybatisMapper<VerificationCodeEntity> {
}
