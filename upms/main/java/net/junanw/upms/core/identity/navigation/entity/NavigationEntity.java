package net.junanw.upms.core.identity.navigation.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.SoftDeletableEntity;

/**
 * 导航实体。
 *
 * <p>用于保存前端导航节点的层级、路由、组件和可见性信息。
 */
@Getter
@Setter
@Table("ui_navigation")
public class NavigationEntity extends SoftDeletableEntity {

    /** 导航主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 上级导航主键。 */
    private Long parentId;

    /** 导航编码。 */
    private String navCode;

    /** 导航名称。 */
    private String navName;

    /** 导航类型。 */
    private String navType;

    /** 路由路径。 */
    private String routePath;

    /** 组件路径。 */
    private String componentPath;

    /** 图标。 */
    private String icon;

    /** 外链地址。 */
    private String externalUrl;

    /** 是否可见。 */
    private Boolean visibleFlag;

    /** 排序号。 */
    private Integer sortOrder;

    /** 导航状态。 */
    private String status;

    /** 备注。 */
    private String remark;
}
