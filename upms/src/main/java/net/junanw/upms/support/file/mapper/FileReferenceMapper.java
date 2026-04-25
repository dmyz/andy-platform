package net.junanw.upms.support.file.mapper;

import com.mybatisflex.core.BaseMapper;
import net.junanw.upms.support.file.entity.FileReferenceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件引用关系 Mapper。
 *
 * <p>提供 FileReference 数据访问能力。
 */
@Mapper
public interface FileReferenceMapper extends BaseMapper<FileReferenceEntity> {
}
