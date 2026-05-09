package net.junanw.upms.business.content.file.mapper;

import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import net.junanw.upms.business.content.file.entity.FileReferenceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件引用关系 Mapper。
 *
 * <p>提供 FileReference 数据访问能力。
 */
@Mapper
public interface FileReferenceMapper extends MybatisMapper<FileReferenceEntity> {
}
