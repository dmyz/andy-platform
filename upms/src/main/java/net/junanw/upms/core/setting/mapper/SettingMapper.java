package net.junanw.upms.core.setting.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.setting.entity.SettingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper
 */
@Mapper
public interface SettingMapper extends MybatisMapper<SettingEntity> {
}
