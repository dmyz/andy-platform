package net.junanw.upms.core.organization.model.view;

/**
 * 组织详情视图。
 *
 * <p>用于承载组织详情接口的返回字段。
 *
 * @param id 组织 ID
 * @param parentId 父级组织 ID
 * @param parentName 父级组织名称
 * @param name 组织名称
 * @param code 组织编码
 * @param leader 负责人
 * @param level 组织层级
 * @param sort 排序号
 * @param status 状态值
 * @param remark 备注
 */

public record OrganizationDetailView(
        String id,
        String parentId,
        String parentName,
        String name,
        String code,
        String leader,
        Integer level,
        Integer sort,
        Integer status,
        String remark
) {
}
