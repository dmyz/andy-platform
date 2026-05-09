package net.junanw.upms.business.content.file.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.business.content.file.entity.StoredFileEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 存储文件 Mapper
 */
@Mapper
public interface StoredFileMapper extends MybatisMapper<StoredFileEntity> {
}
