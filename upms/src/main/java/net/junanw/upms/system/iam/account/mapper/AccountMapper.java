package net.junanw.upms.system.iam.account.mapper;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.system.iam.account.entity.AccountEntity;

/**
 * 账号 Mapper 接口。
 *
 * <p>提供账号标识查询、用户账号读取和批量账号装载能力，是认证与用户资料聚合的重要数据入口。
 */
public interface AccountMapper extends BaseMapper<AccountEntity> {
}
