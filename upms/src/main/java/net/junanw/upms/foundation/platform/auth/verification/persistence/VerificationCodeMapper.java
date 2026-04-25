package net.junanw.upms.foundation.platform.auth.verification.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码 Mapper。
 *
 * <p>负责查询某个目标在指定场景下最近一次未使用验证码，以及批量获取待失效历史验证码。
 */
@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCodeEntity> {
}
