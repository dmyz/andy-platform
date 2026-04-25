package net.junanw.upms.system.dictionary.repository;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.system.dictionary.entity.DictionaryItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典项 Mapper
 */
@Mapper
public interface DictionaryItemMapper extends BaseMapper<DictionaryItemEntity> {
}
