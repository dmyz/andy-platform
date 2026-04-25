package net.junanw.upms.system.iam.user.model.response;

/**
 * 用户导入结果。
 *
 * @param importedCount 新增数量
 * @param updatedCount 更新数量
 * @param skippedCount 跳过数量
 */
public record UserImportResult(
        int importedCount,
        int updatedCount,
        int skippedCount
) {
}
