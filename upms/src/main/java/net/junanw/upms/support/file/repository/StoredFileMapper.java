package net.junanw.upms.support.file.repository;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.support.file.entity.StoredFileEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 存储文件 Mapper
 */
@Mapper
public interface StoredFileMapper extends BaseMapper<StoredFileEntity> {
}
