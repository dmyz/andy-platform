package net.junanw.upms.system.setting.repository;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.system.setting.entity.SettingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper
 */
@Mapper
public interface SettingMapper extends BaseMapper<SettingEntity> {
}
