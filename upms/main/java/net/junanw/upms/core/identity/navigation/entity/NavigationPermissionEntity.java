package net.junanw.upms.core.identity.navigation.entity;

import cn.xbatis.db.annotations.TableField;
import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 导航权限关联实体。
 *
 * <p>用于描述某个导航节点与权限定义之间的关联关系。
 */
@Getter
@Setter
@Table("ui_navigation_permission")
public class NavigationPermissionEntity {

    /** 关联主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 导航主键。 */
    private Long navigationId;

    /** 权限主键。 */
    private Long permissionId;

    /** 创建人主键。 */
    private Long creatorId;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;
}
