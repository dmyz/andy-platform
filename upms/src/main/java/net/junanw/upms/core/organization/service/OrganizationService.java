package net.junanw.upms.core.organization.service;

import net.junanw.upms.core.organization.model.request.OrganizationSaveRequest;
import net.junanw.upms.core.organization.model.view.OrganizationDetailView;
import net.junanw.upms.core.organization.model.view.OrganizationMemberItem;
import net.junanw.upms.core.organization.model.view.OrganizationTreeNode;

import java.util.List;

/**
 * 组织服务接口。
 *
 * <p>定义组织树读取、组织维护、状态更新与成员查询能力。
 */
public interface OrganizationService {

    /** 查询组织树。 */
    List<OrganizationTreeNode> tree(String keyword);

    /** 查询组织详情。 */
    OrganizationDetailView detail(String id);

    /** 创建组织。 */
    OrganizationDetailView create(OrganizationSaveRequest request);

    /** 更新组织。 */
    OrganizationDetailView update(String id, OrganizationSaveRequest request);

    /** 删除组织。 */
    void delete(String id);

    /** 更新组织状态。 */
    void updateStatus(String id, Integer status);

    /** 查询组织成员。 */
    List<OrganizationMemberItem> members(String id, String displayName, Integer status);

    /** 更新用户主组织关系中的岗位名称。 */
    void updatePrimaryPosition(Long userId, String positionName);
}
