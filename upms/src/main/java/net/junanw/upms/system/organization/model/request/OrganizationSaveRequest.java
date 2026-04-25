package net.junanw.upms.system.organization.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 组织保存请求。
 *
 * <p>封装组织新增与编辑接口的请求参数。
 *
 * @param parentId 父级组织 ID
 * @param name 组织名称
 * @param code 组织编码
 * @param leader 负责人
 * @param sort 排序号
 * @param status 状态值
 * @param remark 备注
 */

public record OrganizationSaveRequest(
        String parentId,
        @NotBlank(message = "组织名称不能为空")
        @Size(max = 128, message = "组织名称长度不能超过128")
        String name,
        @NotBlank(message = "组织编码不能为空")
        @Size(max = 64, message = "组织编码长度不能超过64")
        String code,
        @Size(max = 64, message = "负责人长度不能超过64")
        String leader,
        @NotNull(message = "排序号不能为空")
        @Min(value = 1, message = "排序号不能小于1")
        @Max(value = 9999, message = "排序号不能大于9999")
        Integer sort,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
