package net.junanw.upms.system.iam.navigation.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.SoftDeletableEntity;

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
    @Id
    private Long id;

    /** 上级导航主键。 */
    @Column("parent_id")
    private Long parentId;

    /** 导航编码。 */
    @Column("nav_code")
    private String navCode;

    /** 导航名称。 */
    @Column("nav_name")
    private String navName;

    /** 导航类型。 */
    @Column("nav_type")
    private String navType;

    /** 路由路径。 */
    @Column("route_path")
    private String routePath;

    /** 组件路径。 */
    @Column("component_path")
    private String componentPath;

    /** 图标。 */
    @Column("icon")
    private String icon;

    /** 外链地址。 */
    @Column("external_url")
    private String externalUrl;

    /** 是否可见。 */
    @Column("visible_flag")
    private Boolean visibleFlag;

    /** 排序号。 */
    @Column("sort_order")
    private Integer sortOrder;

    /** 导航状态。 */
    @Column("status")
    private String status;

    /** 备注。 */
    @Column("remark")
    private String remark;
}
