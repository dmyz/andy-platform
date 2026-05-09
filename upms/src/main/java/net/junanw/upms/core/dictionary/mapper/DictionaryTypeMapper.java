package net.junanw.upms.core.dictionary.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.dictionary.entity.DictionaryTypeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型 Mapper
 */
@Mapper
public interface DictionaryTypeMapper extends MybatisMapper<DictionaryTypeEntity> {
}
