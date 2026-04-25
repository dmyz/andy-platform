package net.junanw.upms.portal.profile.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 个人资料更新请求。
 *
 * <p>封装个人中心资料更新接口的请求参数。
 *
 * @param realName 真实姓名
 * @param employeeNo 工号
 * @param gender 性别
 * @param orgName 所属组织名称
 * @param positionName 岗位名称
 * @param remark 备注
 */

public record ProfileUpdateRequest(
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 64, message = "真实姓名长度不能超过64")
        String realName,
        @Size(max = 64, message = "工号长度不能超过64")
        String employeeNo,
        @Size(max = 16, message = "性别长度不能超过16")
        String gender,
        @Size(max = 64, message = "所属组织长度不能超过64")
        String orgName,
        @Size(max = 64, message = "岗位长度不能超过64")
        String positionName,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
