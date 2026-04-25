package net.junanw.upms.system.iam.navigation.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 导航保存请求。
 *
 * <p>封装导航新增与编辑接口的请求参数。
 *
 * @param parentId 父级导航 ID
 * @param name 导航名称
 * @param type 导航类型
 * @param routePath 路由路径
 * @param componentPath 前端组件路径
 * @param externalUrl 外链地址
 * @param icon 图标标识
 * @param sortOrder 排序号
 * @param visible 是否显示
 * @param status 状态值
 */

public record NavigationSaveRequest(
        String parentId,
        @NotBlank(message = "名称不能为空")
        @Size(max = 64, message = "名称长度不能超过64")
        String name,
        @NotBlank(message = "导航类型不能为空")
        String type,
        @Size(max = 128, message = "路由路径长度不能超过128")
        String routePath,
        @Size(max = 255, message = "组件路径长度不能超过255")
        String componentPath,
        @Size(max = 255, message = "外链地址长度不能超过255")
        String externalUrl,
        @Size(max = 64, message = "图标长度不能超过64")
        String icon,
        @NotNull(message = "排序号不能为空")
        @Min(value = 1, message = "排序号不能小于1")
        @Max(value = 9999, message = "排序号不能大于9999")
        Integer sortOrder,
        @NotNull(message = "是否显示不能为空")
        Boolean visible,
        @NotNull(message = "状态不能为空")
        Integer status
) {
}
