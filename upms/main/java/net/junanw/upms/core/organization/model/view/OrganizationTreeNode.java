package net.junanw.upms.core.organization.model.view;

import java.util.List;

/**
 * 组织树节点视图。
 *
 * <p>用于承载组织树接口的返回字段。
 *
 * @param id 组织 ID
 * @param parentId 父级组织 ID
 * @param name 组织名称
 * @param code 组织编码
 * @param status 状态值
 * @param sort 排序号
 * @param children 子节点列表
 */

public record OrganizationTreeNode(
        String id,
        String parentId,
        String name,
        String code,
        Integer status,
        Integer sort,
        List<OrganizationTreeNode> children
) {
}
