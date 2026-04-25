package net.junanw.upms.system.iam.user.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 用户保存请求。
 *
 * @param username 用户名
 * @param realName 真实姓名
 * @param jobNumber 工号
 * @param mobile 手机号
 * @param email 邮箱
 * @param gender 性别
 * @param orgId 所属组织主键
 * @param position 岗位名称
 * @param password 初始密码或更新密码
 * @param status 状态，1 表示启用，0 表示停用
 * @param remark 备注
 */
public record UserSaveRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 64, message = "用户名长度不能超过64")
        String username,
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 64, message = "真实姓名长度不能超过64")
        String realName,
        @Size(max = 64, message = "工号长度不能超过64")
        String jobNumber,
        @NotBlank(message = "手机号不能为空")
        @Size(max = 32, message = "手机号长度不能超过32")
        String mobile,
        @Size(max = 128, message = "邮箱长度不能超过128")
        String email,
        @Size(max = 16, message = "性别长度不能超过16")
        String gender,
        @NotBlank(message = "所属组织不能为空")
        String orgId,
        @Size(max = 64, message = "岗位长度不能超过64")
        String position,
        String password,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
