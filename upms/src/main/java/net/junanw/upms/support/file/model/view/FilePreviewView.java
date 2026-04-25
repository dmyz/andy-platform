package net.junanw.upms.support.file.model.view;

/**
 * 文件预览视图。
 *
 * <p>用于承载文件预览接口的返回字段。
 *
 * @param previewUrl 预览地址
 */

public record FilePreviewView(
        String previewUrl
) {
}
