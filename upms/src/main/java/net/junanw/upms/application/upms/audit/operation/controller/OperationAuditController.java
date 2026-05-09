package net.junanw.upms.application.upms.audit.operation.controller;

import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.security.audit.operation.model.view.OperationAuditDetailView;
import net.junanw.upms.core.security.audit.operation.model.view.OperationAuditPageItem;
import net.junanw.upms.core.security.audit.operation.service.OperationAuditService;
import net.junanw.upms.infrastructure.shared.web.DateTimeRangeParser;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OperationAuditController 控制器。
 *
 * <p>负责提供 OperationAudit 相关 HTTP 接口并委托服务层处理具体业务。
 */
@RestController
@RequestMapping("/admin/operation-audit")
public class OperationAuditController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OperationAuditService operationAuditService;

    public OperationAuditController(OperationAuditService operationAuditService) {
        this.operationAuditService = operationAuditService;
    }

    @GetMapping("/page")
    @SaCheckPermission("audit:operation:view")
    /** 查询操作审计分页。 */
    public ApiResponse<PageResponse<OperationAuditPageItem>> page(
            String operatorName,
            String moduleName,
            String actionType,
            String result,
            String startTime,
            String endTime,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(operationAuditService.page(
                operatorName,
                moduleName,
                actionType,
                result,
                DateTimeRangeParser.parseStartTime(startTime),
                DateTimeRangeParser.parseEndTime(endTime),
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("audit:operation:view")
    /** 查询操作审计详情。 */
    public ApiResponse<OperationAuditDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(operationAuditService.detail(id));
    }

    @GetMapping("/export")
    @SaCheckPermission("audit:operation:export")
    /** 导出操作审计 CSV。 */
    public ResponseEntity<byte[]> export(
            String operatorName,
            String moduleName,
            String actionType,
            String result,
            String startTime,
            String endTime
    ) {
        List<OperationAuditPageItem> items = operationAuditService.export(
                operatorName,
                moduleName,
                actionType,
                result,
                DateTimeRangeParser.parseStartTime(startTime),
                DateTimeRangeParser.parseEndTime(endTime)
        );
        StringBuilder builder = new StringBuilder();
        builder.append("操作人,模块名称,操作类型,请求地址,耗时(ms),操作时间,操作结果\n");
        items.forEach(item -> builder
                .append(csv(item.operatorName())).append(',')
                .append(csv(item.moduleName())).append(',')
                .append(csv(item.actionType())).append(',')
                .append(csv(item.requestUri())).append(',')
                .append(item.durationMs() == null ? "\"\"" : item.durationMs()).append(',')
                .append(csv(item.operationTime() == null ? "" : item.operationTime().format(DATE_TIME_FORMATTER))).append(',')
                .append(csv(item.result()))
                .append('\n'));

        byte[] body = ("\uFEFF" + builder).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("operation-audit.csv", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }

    /** 转义 CSV 文本列。 */
    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
