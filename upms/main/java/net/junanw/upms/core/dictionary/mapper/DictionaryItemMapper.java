package net.junanw.upms.core.dictionary.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.core.dictionary.entity.DictionaryItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典项 Mapper
 */
@Mapper
public interface DictionaryItemMapper extends MybatisMapper<DictionaryItemEntity> {
}
