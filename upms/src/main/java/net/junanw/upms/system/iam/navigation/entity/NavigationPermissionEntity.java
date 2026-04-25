package net.junanw.upms.system.iam.navigation.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
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
    @Id
    private Long id;

    /** 导航主键。 */
    @Column("navigation_id")
    private Long navigationId;

    /** 权限主键。 */
    @Column("permission_id")
    private Long permissionId;

    /** 创建人主键。 */
    @Column("creator_id")
    private Long creatorId;

    /** 创建时间。 */
    @Column("created_at")
    private LocalDateTime createdAt;
}
